package com.jaregu.database.queries.ext.spring;

import com.jaregu.database.queries.annotation.Column;
import com.jaregu.database.queries.annotation.Table;

/**
 * Test fixture proving {@code @Column(name = "...")} is honoured by the
 * Spring row mapper. Every column name diverges from what
 * snake_case-to-camelCase would derive — without {@code @Column} support, the
 * Spring default mapper would leave every field {@code null}.
 *
 * <ul>
 *   <li>{@code usr_id} → {@code id} (default would derive {@code usrId})</li>
 *   <li>{@code usr_first_nm} → {@code firstName} (default would derive {@code usrFirstNm})</li>
 *   <li>{@code usr_last_nm} → {@code lastName}</li>
 *   <li>{@code usr_age} → {@code age}</li>
 * </ul>
 */
@Table(name = "app_user")
public class ColumnAwareUser {

	@Column(name = "usr_id")
	private Integer id;

	@Column(name = "usr_first_nm")
	private String firstName;

	@Column(name = "usr_last_nm")
	private String lastName;

	@Column(name = "usr_age")
	private Integer age;

	public ColumnAwareUser() {
	}

	public ColumnAwareUser(Integer id, String firstName, String lastName, Integer age) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }

	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }

	public Integer getAge() { return age; }
	public void setAge(Integer age) { this.age = age; }
}
