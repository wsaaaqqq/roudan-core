package org.xht.xdb.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class SaveOrUpdateBatchResult<T> {
    private Collection<T> saveList = new ArrayList<>();
    private Collection<T> updateList = new ArrayList<>();
}
