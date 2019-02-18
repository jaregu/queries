package com.jaregu.database.queries.ext.dalesbred;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.dalesbred.result.ResultTable;
import org.dalesbred.result.ResultTable.ColumnMetadata;
import org.dalesbred.result.ResultTable.ResultRow;

import com.google.common.base.CaseFormat;

public class ResultTableConverter {

	public static List<Map<String, Object>> toListMap(ResultTable resultTable) {
		List<Map<String, Object>> resultList = new ArrayList<>(resultTable.getRowCount());
		List<ColumnMetadata> columns = resultTable.getColumns();
		List<String> mapNames = new ArrayList<>(columns.size());
		for (int i = 0; i < columns.size(); i++) {
			mapNames.add(
					CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columns.get(i).getName().toLowerCase()));
		}

		for (ResultRow resultRow : resultTable) {
			Map<String, Object> rowMap = new LinkedHashMap<>(resultTable.getColumnCount());
			resultList.add(rowMap);
			for (int i = 0; i < columns.size(); i++) {
				rowMap.put(mapNames.get(i), resultRow.get(i));
			}
		}
		return resultList;
	}

	public static DataTable toDataTable(ResultTable resultTable) {
		List<String> columns = resultTable.getColumns().stream().map(c -> c.getName()).collect(Collectors.toList());
		List<List<Object>> data = resultTable.getRows().stream()
				.map(r -> StreamSupport.stream(r.spliterator(), false).collect(Collectors.toList()))
				.collect(Collectors.toList());
		return DataTable.create(columns, data);
	}

	public static DataTable toDataTable(ResultTable resultTable, Function<List<Object>, List<Object>> rowUpdater) {
		List<String> columns = resultTable.getColumns().stream().map(c -> c.getName()).collect(Collectors.toList());
		List<List<Object>> data = resultTable.getRows().stream()
				.map(r -> StreamSupport.stream(r.spliterator(), false).collect(Collectors.toList()))
				.map(r1 -> rowUpdater.apply(r1)).collect(Collectors.toList());
		return DataTable.create(columns, data);
	}
}
