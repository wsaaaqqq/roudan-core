package org.xht.xdb.vo;

import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCompareResult<T> {
    private List<T> dataForInsert;
    private List<T> dataForUpdate;
    private List<T> dataForDelete;
}
