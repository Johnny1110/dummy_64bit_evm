package com.frizo.lab.sevm.compiler.req;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatchRequest {
    private int pos;
    private String label;
}
