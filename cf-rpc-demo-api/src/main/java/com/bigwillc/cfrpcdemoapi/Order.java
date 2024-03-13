package com.bigwillc.cfrpcdemoapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bigwillc on 2024/3/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    Integer id;
    Float amount;
}
