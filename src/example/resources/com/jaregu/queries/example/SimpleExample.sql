-- find all
select * from person;

-- count persons
select count(1) person_count from person;

/* persons search */
select p.*
from person p
where 1 = 1
	-- so this is just comment, comments which doesn't look like expressions are ignored
	
	-- line below will be added if firstName parameter is not null
	AND p.first_name = '' -- :firstName
	
	-- expressions for conditional argument lines consists of value_expression [; conditional_expression]
	-- where conditional_expression is optional, if it is ommited, all variables used in value_expression
	-- is tested to be non null, if they are, argument line is added
	
	
	-- line below will be added to resulting query if firstName2 is not null and is not empty string
	AND (p.first_name like '%' /* :firstName2 + '%'; :firstName2 != null && :firstName2 != '' */)
	-- resulting binding value for first_name criteria will be firstName2 concatenated with '%'
	
	-- line below will replace ?, it can replace string or number constants
	AND (LOWER(p.first_name) = LOWER(? /* :firstName3 */))
	-- resulting binding value for first_name criteria will be firstName2 concatenated with '%'
	
	-- line below will be added allways, you can allways switch some expression comment off with comment starting with /** or ---
	AND 2 = 2 /** :someValue */
	
	-- both below added lines will be added if text parameter will be non null, non empty string
	AND (p.first_name like '%' -- :text + '%'; :text != null && :text != ''
		OR p.last_name like '%' /* :text + '%'; :text != null && :text != '' */)
		
	
order by p.first_name, p.last_name
limit :offset, :pageSize -- both offset and pageSize parameters are mandatory, so they have to be non null
;
