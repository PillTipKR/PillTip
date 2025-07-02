/*
	해당 폴더 및 하위 폴더의 모든 페이지에 공통적으로 적용되는 레이아웃
	- example 폴더 내의 모든 페이지에 공통 패딩을 적용
	- 반응형 디자인을 위한 기본 레이아웃 구조 제공
*/
import styles from "./layout.module.css";

// Props 인터페이스 정의 - children prop을 받아서 레이아웃 내부에 렌더링
interface Props {
  children: React.ReactNode;
}

// 예제 페이지들의 공통 레이아웃 컴포넌트
export default function Layout({ children }: Props) {
  // 패딩 클래스를 적용하여 하위 컴포넌트들을 감싸는 컨테이너 반환
  return <div className={styles.padding}>{children}</div>;
}
