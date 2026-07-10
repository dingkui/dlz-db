package com.dlz.db.convertor.columnname;

import java.util.Locale;

/**
 * 驼峰命名与数据库字段名互转
 *
 * 转换规则：
 *
 * Java -> DB
 * userName -> user_name
 * userID   -> user_i_d
 * URLValue -> u_r_l_value
 * ABC      -> a_b_c
 *
 * DB -> Java
 * user_name -> userName
 * user_i_d  -> userID
 * a_b_c     -> aBC（如果用于字段名）
 *
 * 特点：
 * 1. 转换规则简单
 * 2. 基本可逆
 * 3. 不依赖正则，性能更高
 */
public class NameConvertCamel implements INameConverter {

	/**
	 * 数据库字段名转为Java字段名（DB -> Java）
	 *
	 * 规则：
	 * 1. 无下划线时，若字符串中包含任意小写字母则原样返回；否则整体转小写。
	 * 2. 含下划线时，先整体转小写，再将每个下划线后的字母大写并移除下划线。
	 *
	 * 测试用例（输入 -> 输出）：
	 * 1、下划线转驼峰
	 *   user_name        -> userName
	 * 2、全小写无下划线（原样返回）
	 *   name             -> name
	 * 3、全大写无下划线（整体转小写）
	 *   DELETED          -> deleted
	 * 4、大写带下划线
	 *   USER_NAME        -> userName
	 * 5、混合大小写无下划线（含小写则原样返回）
	 *   UserName         -> UserName
	 * 6、包含数字
	 *   user1_name       -> user1Name
	 *   user123          -> user123
	 * 7、多个连续下划线
	 *   user__name       -> userName
	 *   user_name__info  -> userNameInfo
	 * 8、开头/结尾下划线
	 *   _user_name       -> UserName
	 *   user_name_       -> userName
	 *   _user_name_      -> UserName
	 * 9、边界
	 *   ""               -> ""
	 *   null             -> ""
	 * 其他示例：
	 *   user_i_d         -> userID
	 *   a_b_c            -> aBC
	 *
	 * @param name
	 * @return
	 */
	@Override
	public String toFieldName(String name) {
		if (name == null) {
			return "";
		}

		if (!name.contains("_")) {
			//3、全大写无下划线（整体转小写）  DELETED  -> deleted
			for (int i = 0; i < name.length(); i++) {
				if (Character.isLowerCase(name.charAt(i))) {
					return name;
				}
			}
			return name.toLowerCase(Locale.ROOT);
		}

		name = name.toLowerCase(Locale.ROOT);

		StringBuilder sb = new StringBuilder(name.length());
		boolean upper = false;

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);

			if (c == '_') {
				upper = true;
				continue;
			}

			if (upper) {
				sb.append(Character.toUpperCase(c));
				upper = false;
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/**
	 * Java字段名转为数据库字段名（Java -> DB）
	 *
	 * 规则：
	 * 1. 含下划线时，直接整体转小写返回。
	 * 2. 不含下划线时，遇到大写字母在其前插入下划线并转小写（首字母大写也会插下划线）。
	 *
	 * 测试用例（输入 -> 输出）：
	 * 1、全小写（原样返回）
	 *   name             -> name
	 * 2、基本驼峰转下划线
	 *   userName         -> user_name
	 * 3、已含下划线（整体转小写）
	 *   user_name        -> user_name
	 *   USER_ID          -> user_id
	 * 4、连续大写字母
	 *   userID           -> user_i_d
	 * 5、包含数字
	 *   user1Name        -> user1_name
	 *   user123          -> user123
	 * 6、首字母大写（大写前插下划线，含首字母）
	 *   UserName         -> _user_name
	 *   User             -> _user
	 *   userName         -> user_name
	 * 7、边界
	 *   ""               -> ""
	 *   null             -> null
	 *
	 * @param name
	 * @return
	 */
	@Override
	public String toDbName(String name) {
		if (name == null) {
			return null;
		}

		if (name.indexOf('_') >= 0) {
			return name.toLowerCase(Locale.ROOT);
		}

		StringBuilder sb = new StringBuilder(name.length() + 8);

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append('_');
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}
}