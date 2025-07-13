"use client";

import React, { createContext, useEffect, useState } from "react";
import styles from "./layout.module.css";

// 서버-클라이언트 시간차 Context 생성
export const ServerTimeDiffContext = createContext<number>(0);

interface Props {
  children: React.ReactNode;
}

// 서버 시간차 구하는 함수
async function getServerTimeDiff() {
  try {
    const res = await fetch("/api/server-time");
    const { serverTime } = await res.json();
    const clientTime = Date.now();
    return serverTime - clientTime;
  } catch {
    return 0;
  }
}

export default function Layout({ children }: Props) {
  const [serverTimeDiff, setServerTimeDiff] = useState(0);

  useEffect(() => {
    getServerTimeDiff().then(setServerTimeDiff);
  }, []);

  return (
    <ServerTimeDiffContext.Provider value={serverTimeDiff}>
      <div className={styles.padding}>{children}</div>
    </ServerTimeDiffContext.Provider>
  );
}
