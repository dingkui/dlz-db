package com.dlz.db.helper.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Condition {
	String column;
	String operation;
	Object value;
}
