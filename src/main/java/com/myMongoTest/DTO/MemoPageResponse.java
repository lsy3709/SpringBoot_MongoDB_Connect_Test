package com.myMongoTest.DTO;

import java.util.List;

import com.myMongoTest.document.Memo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 커서 기반 페이지네이션 응답.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoPageResponse {
    private List<Memo> list;
    private boolean hasNext;
}
