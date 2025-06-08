package com.oauth2.Drug.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oauth2.Drug.Repository.DrugRepository;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Domain.DrugEffect;
import com.oauth2.Drug.Domain.DrugIngredient;
import com.oauth2.Drug.Domain.DrugStorageCondition;
import com.oauth2.Drug.Domain.Ingredient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DrugImportService {
    private final DrugService drugService;
    private final IngredientService ingredientService;
    private final DrugIngredientService drugIngredientService;
    private final DrugEffectService drugEffectService;
    private final DrugStorageConditionService drugStorageConditionService;
    private final DrugRepository drugRepository;

    // TODO: 나머지 서비스도 필요시 주입

    public DrugImportService(DrugService drugService, IngredientService ingredientService, DrugIngredientService drugIngredientService, DrugEffectService drugEffectService, DrugStorageConditionService drugStorageConditionService, DrugRepository drugRepository) {
        this.drugService = drugService;
        this.ingredientService = ingredientService;
        this.drugIngredientService = drugIngredientService;
        this.drugEffectService = drugEffectService;
        this.drugStorageConditionService = drugStorageConditionService;
        this.drugRepository = drugRepository;
    }

    public void importFromFile(String filePath) {
        try {
            String content = Files.readString(Paths.get(filePath));
            String[] lines = content.split("\n"); // 전체 내용에서 라인별로 나누기
            StringBuilder currentBlock = new StringBuilder(); // 현재 블럭을 저장할 변수
            System.out.println("약품DB 삽입 시작");
            for (String line : lines) {
                line = line.trim(); // 공백 제거

                if (line.contains("제품명:")) {
                    // 이전 블럭이 비어있지 않으면 현재까지의 블럭 처리
                    if (!currentBlock.isEmpty()) {
                        try {
                            importSingleDrug(currentBlock.toString()); // 현재 블럭 데이터 처리
                        } catch (Exception e) {
                            System.out.println("약 데이터 저장 실패: " + e.getMessage());
                        }
                    }

                    currentBlock.setLength(0); // 현재 블럭 초기화
                    currentBlock.append(line); // 새로운 블럭 첫 줄 추가
                } else {
                    // '제품명:'이 포함되지 않으면 현재 블럭에 해당 라인 추가
                    currentBlock.append("\n").append(line);
                }
            }

            // 마지막 블럭 처리 (루프 끝에서 남아있는 블럭 처리)
            if (!currentBlock.isEmpty()) {
                try {
                    importSingleDrug(currentBlock.toString());
                } catch (Exception e) {
                    System.out.println("약 데이터 저장 실패: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void importSingleDrug(String block) {
        // 1. 기본 정보 파싱
        String[] lines = block.split("\n");
        String name = null, code = null, manufacturer = null, approvalDate = null, packaging = null, form = null, atcCode = null, tag = null;
        String ingredientLine = null;
        StringBuilder effect = new StringBuilder();
        StringBuilder usage = new StringBuilder();
        StringBuilder caution = new StringBuilder();
        String section = "";
        for (String line : lines) {
            line = line.trim();
            if (line.contains("제품명:")) name = line.split("제품명:", 2)[1].trim();
            else if (line.contains("품목일련번호:")) code = line.split("품목일련번호:", 2)[1].trim();
            else if (line.contains("제조사:")) manufacturer = line.split("제조사:", 2)[1].trim();
            else if (line.contains("허가일자:")) approvalDate = line.split("허가일자:", 2)[1].trim();
            else if (line.contains("포장단위:")) packaging = line.split("포장단위:", 2)[1].trim();
            else if (line.contains("제형:")) form = line.split("제형:", 2)[1].trim();
            else if (line.contains("ATC 코드:")) atcCode = line.split("ATC 코드:", 2)[1].trim();
            else if (line.contains("의약품 분류:")) tag = line.split("의약품 분류:", 2)[1].trim();
            else if (line.contains("약품 성분 및 용량:")) ingredientLine = line.split("약품 성분 및 용량:", 2)[1].trim();
            else if (line.startsWith("[효능효과]")) section = "effect";
            else if (line.startsWith("[용법용량]")) section = "usage";
            else if (line.startsWith("[사용상의주의사항]") || line.startsWith("[사용상주의사항]")) section = "caution";
            else {
                switch (section) {
                    case "effect" -> effect.append(line).append("\n");
                    case "usage" -> usage.append(line).append("\n");
                    case "caution" -> caution.append(line).append("\n");
                }
            }
        }
        // name이 없으면 Drug 저장 건너뜀
        if (name == null || name.isEmpty()) {
            System.out.println("제품명 누락 블록 건너뜀: " + block);
            return;
        }
        // 이미 존재하는 약이면 스킵
        if(drugRepository.findByName(name).isPresent()) return;

        // 2. Drug 저장 (제품명 기준 중복 체크)
        Optional<Drug> drugOpt = drugService.findByName(name);
        Drug drug;
        if (drugOpt.isPresent()) {
            drug = drugOpt.get();
        } else {
            drug = new Drug();
            drug.setName(name);
            drug.setCode(code);
            drug.setManufacturer(manufacturer);
            drug.setPackaging(packaging);
            drug.setForm(form);
            drug.setAtcCode(atcCode);
            // approvalDate 파싱 및 세팅
            if (approvalDate != null && approvalDate.matches("\\d{8}")) {
                try {
                    LocalDate localDate = LocalDate.parse(approvalDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    drug.setApprovalDate(Date.valueOf(localDate));
                } catch (Exception e) {
                    System.out.println("허가일자 파싱 오류: " + approvalDate);
                }
            }
            if (tag != null) drug.setTag(tag.equals("일반의약품")? Drug.Tag.COMMON: Drug.Tag.EXPERT);
            drug = drugService.save(drug);
        }
        // 3. 성분 파싱 및 저장
        if (ingredientLine != null && !ingredientLine.isEmpty()) {
            String[] ingredients = ingredientLine.split(";");
            for (String ing : ingredients) {
                String[] parts = ing.split("\\|");
                String nameKr = null, amount = "", unit = null;
                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("성분명 :")) nameKr = part.replace("성분명 :", "").trim();
                    else if (part.startsWith("분량 :")) amount = part.replace("분량 :", "").trim();
                    else if (part.startsWith("단위 :")) unit = part.replace("단위 :", "").trim();
                }
                if (nameKr == null) continue;
                Optional<Ingredient> ingOpt = ingredientService.findByNameKr(nameKr);
                Ingredient ingredient;
                if (ingOpt.isPresent()) {
                    ingredient = ingOpt.get();
                } else {
                    ingredient = new Ingredient();
                    ingredient.setNameKr(nameKr);
                    ingredient.setNameEn("v");
                    ingredient = ingredientService.save(ingredient);
                }

                // DrugIngredient 매핑 저장
                DrugIngredient di = new DrugIngredient();
                DrugIngredient.DrugIngredientId diId = new DrugIngredient.DrugIngredientId();
                diId.setDrugId(drug.getId());
                diId.setIngredientId(ingredient.getId());
                di.setId(diId);
                parseIngredientAmount(di, amount);
                di.setUnit(unit);
                drugIngredientService.save(di);
            }
        }
        // 4. DrugEffect 저장 (표 등 긴 데이터 방지)
        String filteredEffect = filterContent(effect.toString());
        String filteredUsage = filterContent(usage.toString());
        String filteredCaution = filterContent(caution.toString());
        if (!filteredEffect.isEmpty()) {
            DrugEffect de = new DrugEffect();
            de.setDrug(drug);
            de.setType(DrugEffect.Type.EFFECT);
            de.setContent(filteredEffect.trim());
            drugEffectService.save(de);
        }
        if (!filteredUsage.isEmpty()) {
            DrugEffect de = new DrugEffect();
            de.setDrug(drug);
            de.setType(DrugEffect.Type.USAGE);
            de.setContent(filteredUsage.trim());
            drugEffectService.save(de);
        }
        if (!filteredCaution.isEmpty()) {
            DrugEffect de = new DrugEffect();
            de.setDrug(drug);
            de.setType(DrugEffect.Type.CAUTION);
            de.setContent(filteredCaution.trim());
            drugEffectService.save(de);
        }
        // 5. 보관 방법 파싱 및 저장
        if (!filteredCaution.isEmpty()) {
            // 개행 문자 처리 로직 개선
            String[] cautionLines = filteredCaution.split("\\n");
            for (String cautionLine : cautionLines) {
                String line = cautionLine.trim();
                // 보관 관련 키워드 포함 여부 검사
                if (line.contains("닿지 않는 곳에 보관")) continue;
                if (!line.contains("보관") && !line.contains("바꾸어 넣지")) continue;
                if (line.contains("바꾸어 넣지") || line.contains("원래의 용기") || line.contains("원래 용기") || line.contains("다른 용기")) {
                    saveStorageCondition("용기변화",drug, DrugStorageCondition.Category.CONTAINER,false);
                    continue;
                }
                // 긍정/부정 연결어 패턴\\
                String regex = "보관(?:하지 ?않(?:고|으며|게|을 것|말 것|는다|도록|말고|아야 한다)?|하지 말 것|하지 말아야 한다|하면 안된다|하지 말고|하며|하고|하도록|한다|할 것|하여야 한다)?";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);
                int lastPos = 0;
                boolean lastNegative = false; // 기본값
                while (matcher.find()) {
                    String part = line.substring(lastPos, matcher.start());
                    //System.out.println(matcher.group());
                    boolean isNegative = matcher.group().contains("보관하지") || matcher.group().contains("보관하면 안된다") || lastNegative;
                    // 키워드별 저장
                    makeStoreRule(drug, part, isNegative);
                    lastPos = matcher.end();
                    String matched = matcher.group();
                    lastNegative = matched.startsWith("보관하지") || matched.startsWith("보관하면 안된다");
                }
            }
        }
    }

    private void makeStoreRule(Drug drug, String part, boolean isNegative) {
        // 보관 용기 파싱
        if (part.contains("밀폐용기") || part.contains("밀페하여")) {
            saveStorageCondition("밀폐용기",drug, DrugStorageCondition.Category.CONTAINER,isNegative);
        }
        // 보관 장소 파싱
        if (part.contains("밀폐된 장소")) {
            saveStorageCondition("밀폐된 장소",drug, DrugStorageCondition.Category.PLACE,isNegative);
        }
        if (part.contains("서늘한 곳")) {
            saveStorageCondition("서늘한 곳",drug, DrugStorageCondition.Category.PLACE,isNegative);
        }

        // 온도 파싱
        Matcher m = Pattern.compile("(\\d+)+℃").matcher(part);
        while (m.find()) {
            String tempStr = m.group(1);
            int temp = Integer.parseInt(tempStr);
            String tempType = (temp >= 40) ? "고온" : (temp <= 10 ? "저온" : "실온");
            saveStorageCondition(tempType,drug, DrugStorageCondition.Category.TEMPERATURE,isNegative);
        }
        if (part.contains("고온")) {
            saveStorageCondition("고온",drug, DrugStorageCondition.Category.TEMPERATURE,isNegative);
        }
        if (part.contains("실온")) {
            saveStorageCondition("실온",drug, DrugStorageCondition.Category.TEMPERATURE,isNegative);
        }
        if (part.contains("저온")) {
            saveStorageCondition("저온",drug, DrugStorageCondition.Category.TEMPERATURE,isNegative);
        }
        if (part.contains("냉장")) {
            saveStorageCondition("냉장",drug, DrugStorageCondition.Category.TEMPERATURE,isNegative);
        }
        if (part.contains("냉동")) {
            saveStorageCondition("냉동",drug, DrugStorageCondition.Category.TEMPERATURE,isNegative);
        }
        if (part.contains("얼리지")) {
            saveStorageCondition("냉동x",drug, DrugStorageCondition.Category.TEMPERATURE,isNegative);
        }

        // 습도 파싱
        if (part.contains("건조")) {
            saveStorageCondition("건조",drug, DrugStorageCondition.Category.HUMID,isNegative);
        }
        if (part.contains("습기가 적은")) {
            saveStorageCondition("습기가 적은",drug, DrugStorageCondition.Category.HUMID,isNegative);
        }

        // 직사광선
        if (((part.contains("직사광선") || part.contains("직사일광")) && part.contains("피하여")) || part.contains("차광")) {
            saveStorageCondition("직사광선",drug, DrugStorageCondition.Category.LIGHT,isNegative);
        }
    }

    private String filterContent(String content) {
        // 개행 정리 + 앞뒤 공백 제거
        return content
                .replaceAll("(\\n\\s*){2,}", "\n")  // 2줄 이상 연속 개행 -> 1줄 개행
                .replaceAll(" +", " ")              // 중복 공백 제거
                .replaceAll("={60,}","")
                .trim();
    }


    private void saveStorageCondition(String q, Drug drug,
                                      DrugStorageCondition.Category category, boolean isNegative){
        DrugStorageCondition cond = new DrugStorageCondition();
        cond.setDrug(drug);
        cond.setCategory(category);
        cond.setValue(q);
        cond.setActive(!isNegative && !q.equals("직사광선") && !q.equals("용기변화"));
        drugStorageConditionService.save(cond);
    }

    private void parseIngredientAmount(DrugIngredient di,String amount) {
        // 1. 숫자와 단위를 분리하고 불필요한 공백이나 특수문자 제거
        amount = amount.trim();

        // 연속된 숫자 처리 (예: 1,0001,000 -> 1,000으로 처리)
        if (amount.length() > 1 &&
                amount.substring(0,amount.length()/2)
                        .equals(amount.substring(amount.length()/2))) {
            amount = amount.substring(0, amount.length()/2); // 첫 번째 숫자까지만 추출
        }
        amount = removeDuplicateAmount(amount);
        String numericAmountBackup = amount;
        // 숫자만 남기기 (콤마 제거, "이상" 같은 불필요한 단어 처리)
        String numericAmount = amount.replaceAll("[^0-9.:~]", "").replaceAll("\\.{2,}","");


        // '이상', '역가'와 같은 특수 단위 처리
        if (numericAmount.contains("이상") || numericAmount.contains("역가")) {
            // "이상"이나 "역가"와 같은 단위는 무시하거나 별도의 처리
            numericAmount = numericAmount.replaceAll("이상|역가", "").trim();
        }

        try {
            // 숫자형 데이터로 변환
            if (!numericAmount.isEmpty()) {
                di.setAmount(Float.parseFloat(numericAmount));
                di.setAmountBackup(numericAmountBackup);
            }
        } catch (NumberFormatException e) {
            // 숫자 형식 오류 처리
            //System.out.println("성분 분량 파싱 오류: " + numericAmount);
            di.setAmountBackup(numericAmountBackup);
        }
    }

    // 숫자와 단위가 반복되는 경우를 처리하는 메서드
    private String removeDuplicateAmount(String amount) {
        // 숫자 뒤에 단위가 두 번 이상 반복되는 경우, 첫 번째 값만 유지하고 두 번째 값을 제거
        // 예: 7.953mg7.953 -> 7.953mg
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)([a-zA-Z]+)(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(amount);

        // 만약 단위가 뒤에 반복되면 이를 제거
        if (matcher.find()) {
            amount = matcher.replaceAll("$1");  // 첫 번째 숫자와 단위만 남김
        }

        return amount;
    }

    public void importImageFromCsv(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            reader.readNext(); // skip header
            List<Drug> drugList = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                String nameKr = line[1].trim();
                String imageUrl = line[5].trim().toLowerCase();

                List<Drug> drugs = drugRepository.findByNameContaining(nameKr);
                if (!drugs.isEmpty()) {
                    Drug drug = drugs.get(0);
                    drug.setImage(imageUrl);
                    drugList.add(drug);
                }
            }

            drugRepository.saveAll(drugList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}