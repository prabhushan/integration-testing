package com.java.bytes.external;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class User {
	public int id;
	public String name;
	public String username;
	public String email;
	public String phone;
}

