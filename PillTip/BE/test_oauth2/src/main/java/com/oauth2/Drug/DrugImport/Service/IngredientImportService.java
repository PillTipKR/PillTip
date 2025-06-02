package com.oauth2.Drug.DrugImport.Service;

import com.oauth2.Drug.Domain.*;
import com.oauth2.Drug.Repository.*;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientImportService {

    private final IngredientRepository ingredientRepository;
    private final DrugCautionRepository ducationRepository;
    private final DrugTherapeuticDupRepository drugTherapeuticDupRepository;
    private final IngredientGroupRepository ingredientGroupRepository;
    private final IngredientGroupMemberRepository ingredientGroupMemberRepository;
    private final DrugInteractionRepository interactionRepository;

    public void importIngredientsFromCsv(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            reader.readNext(); // skip header
            List<Ingredient> ingredientList = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                String nameKr = line[0].trim();
                String nameEn = line[4].trim().toLowerCase();

                Optional<Ingredient> ing = ingredientRepository.findByNameKr(nameKr);

                if (ing.isPresent()) {
                    ing.get().setNameEn(nameEn);
                    ingredientList.add(ing.get());
                }
            }

            ingredientRepository.saveAll(ingredientList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //캡슐-캅셀 동의어
    //제형/제품명에 캡슐/캅셀 존재

    //정제
    //보통 필름코팅정 과같이 제형에 표현됨
    // 당의 제피 정제 필름코팅정 필름 코팅 정 - 정제
    // 장용 - 장용정제
    // 서방정 서방성 SR ER - 서방형정제 (가끔 장용성 당의성 등이 붙음)
    //아마 제품명에도 ~정과 같이 표기되어있음

    //시럽제
    //제품명/제형에 시럽이라고 표기

    //점안액
    //제품명에 표기됨

    //과립제
    //과립 -제형에 표기

    //크림제
    //크림 - 제형에 표기

    //질정
    //질정 질좌제 질용정제 - 제형에 표기

    //주사액
    //제품명/제형에 주사제 혹은 제형/포장단위에 바이알
    //이름 뒤에 -주, -주사 -주사액 등으로 끝남 가끔 -주###그램 등 용량이 붙음
    //멸균 정맥 수액

    //점비액
    //점비 -제형에표기

    //액제
    //주사액, 점안액, 점비액, 캡슐이 아닌 나머지 액제

    //연고제
    //연고 -제형에표기

    //환제
    //환제 -제형에표기

    //겔제
    //겔 -제형에표기

    public void importCautionsFromCsv(MultipartFile file, DrugCaution.ConditionType type) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readNext(); // skip header
            String[] line;
            List<DrugCaution> cautionList = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                String nameEn = line[0].trim();
                String dosageForm = line.length == 3 || line.length == 4 ? line[1].trim() : "";
                String condition = line.length==4? line[2].trim(): "";

                String note = line.length == 2 ? line[1].trim() : "";
                if(line.length == 3) note = line[2].trim();
                else if(line.length == 4) note = line[3].trim();

                String approval = line.length == 5? line[4].trim() : "";

                List<Ingredient> ingredientOpt = ingredientRepository.findByNameEn(nameEn);
                for(Ingredient ing : ingredientOpt) {
                    DrugCaution caution = new DrugCaution();
                    caution.setIngredientId(ing.getId());
                    caution.setConditionType(type);
                    caution.setDosageForm(dosageForm);
                    caution.setConditionValue(condition);
                    caution.setNote(note);
                    caution.setApprovalInfo(approval);
                    cautionList.add(caution);
                }
            }

            ducationRepository.saveAll(cautionList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importTherapeuticsFromCsv(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readNext(); // skip header
            String[] line;
            List<DrugTherapeuticDup> drugTherapeuticDupList = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                String nameEn = line[2].trim();
                String category = line[0].trim();
                String className = line[1].trim();
                String note = line.length == 4 ? line[3].trim() : "";
                List<Ingredient> ingredientOpt = ingredientRepository.findByNameEn(nameEn);
                for(Ingredient ing : ingredientOpt) {
                    DrugTherapeuticDup drugTherapeuticDup = new DrugTherapeuticDup();
                    drugTherapeuticDup.setIngredientId(ing.getId());
                    drugTherapeuticDup.setCategory(category);
                    drugTherapeuticDup.setClassName(className);
                    drugTherapeuticDup.setNote(note);

                    drugTherapeuticDupList.add(drugTherapeuticDup);
                }
            }

            drugTherapeuticDupRepository.saveAll(drugTherapeuticDupList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importInteractionsFromCsv(MultipartFile file) {
        try (
                CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
        ) {
            reader.readNext(); // Skip header
            String[] line;

            while ((line = reader.readNext()) != null) {
                String fullExpr = line[0].trim(); // 전체 조합
                String expr1 = line[1].trim();    // 왼쪽 복합 성분명
                String expr2 = line[2].trim();    // 오른쪽 복합 성분명
                String reason = line[3].trim();
                String note = line.length >= 6 ? line[5].trim() : "";

                List<List<Long>> leftIdSets = parseExprToIngredientIdSets(expr1);
                List<List<Long>> rightIdSets = parseExprToIngredientIdSets(expr2);

                List<List<Long>> leftCombos = cartesianProduct(leftIdSets);
                List<List<Long>> rightCombos = cartesianProduct(rightIdSets);

                for (List<Long> leftCombo : leftCombos) {
                    Long groupId1 = createGroupIfNotExists(leftCombo);

                    for (List<Long> rightCombo : rightCombos) {
                        Long groupId2 = createGroupIfNotExists(rightCombo);

                        DrugInteraction interaction = new DrugInteraction();
                        interaction.setGroupId1(groupId1);
                        interaction.setGroupId2(groupId2);
                        interaction.setReason(reason);
                        interaction.setNote(note + "\n | 전체조합: " + fullExpr);
                        interactionRepository.save(interaction);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<List<Long>> parseExprToIngredientIdSets(String expr) {
        List<List<Long>> result = new ArrayList<>();
        if (expr == null || expr.isEmpty()) return result;

        String[] names = expr.split("[+,]");
        for (String name : names) {
            name = name.trim();
            List<Ingredient> ingredients = ingredientRepository.findByNameEn(name);
            List<Long> ids = ingredients.stream().map(Ingredient::getId).toList();
            if (!ids.isEmpty()) result.add(ids);
        }
        return result;
    }

    private List<List<Long>> cartesianProduct(List<List<Long>> lists) {
        List<List<Long>> result = new ArrayList<>();
        if (lists == null || lists.isEmpty()) return result;

        result.add(new ArrayList<>()); // seed with empty list
        for (List<Long> pool : lists) {
            List<List<Long>> newResult = new ArrayList<>();
            for (List<Long> prefix : result) {
                for (Long item : pool) {
                    List<Long> newCombo = new ArrayList<>(prefix);
                    newCombo.add(item);
                    newResult.add(newCombo);
                }
            }
            result = newResult;
        }
        return result;
    }

    private Long createGroupIfNotExists(List<Long> ingredientIds) {
        List<Long> sortedIds = new ArrayList<>(ingredientIds);
        Collections.sort(sortedIds);
        String key = sortedIds.stream().map(String::valueOf).collect(Collectors.joining("+"));

        Optional<IngredientGroup> existing = ingredientGroupRepository.findByGroupName(key);
        if (existing.isPresent()) return existing.get().getGroupId();

        IngredientGroup group = new IngredientGroup();
        group.setGroupName(key);
        group = ingredientGroupRepository.save(group);

        for (Long ingId : sortedIds) {
            IngredientGroupMember member = new IngredientGroupMember();
            member.setGroupId(group.getGroupId());
            member.setIngredientId(ingId);
            ingredientGroupMemberRepository.save(member);
        }

        return group.getGroupId();
    }



}
