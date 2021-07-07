package com.hikvision.redis.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author renpeiqian
 * @date 2021/6/25 15:07
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = -7189462055820600125L;
    private String name;
    private String password;
    private int age;
    private Date birthday;
}
