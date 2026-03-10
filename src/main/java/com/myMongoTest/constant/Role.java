package com.myMongoTest.constant;

public enum Role {
    USER,    // 미승인 일반 유저
    ADUSER,  // 승인된 유저 (인벤토리 관리만 가능)
    ADMIN    // 관리자 (인벤토리 + 회원 관리)
}