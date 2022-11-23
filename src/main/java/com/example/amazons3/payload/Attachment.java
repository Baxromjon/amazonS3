package com.example.amazons3.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attachment {

    private byte[] bytes;

    private String contentType;

    private String name;

    private long size;
}
