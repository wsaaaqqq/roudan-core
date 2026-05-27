package org.xht.xdb.orm.listener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityListener<T> {
    private boolean sync = false;
    private Consumer<T> listener;
}
