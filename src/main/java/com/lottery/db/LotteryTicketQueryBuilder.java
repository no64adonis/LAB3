package com.lottery.db;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LotteryTicketQueryBuilder {
    private StringBuilder query;
    private List<Object> parameters;
    private boolean hasWhereClause;

    public LotteryTicketQueryBuilder(String baseQuery) {
        this.query = new StringBuilder(baseQuery);
        this.parameters = new ArrayList<>();
        this.hasWhereClause = false;
    }

    public LotteryTicketQueryBuilder addCondition(String column, Object value) {
        if (value != null) {
            if (!hasWhereClause) {
                query.append(" WHERE ");
                hasWhereClause = true;
            } else {
                query.append(" AND ");
            }
            query.append(column).append(" = ?");
            parameters.add(value);
        }
        return this;
    }

    public LotteryTicketQueryBuilder addNullCondition(String column) {
        if (!hasWhereClause) {
            query.append(" WHERE ");
            hasWhereClause = true;
        } else {
            query.append(" AND ");
        }
        query.append(column).append(" IS NULL");
        return this;
    }

    public LotteryTicketQueryBuilder addLikeCondition(String column, String value) {
        if (value != null && !value.isEmpty()) {
            if (!hasWhereClause) {
                query.append(" WHERE ");
                hasWhereClause = true;
            } else {
                query.append(" AND ");
            }
            query.append(column).append(" LIKE ?");
            parameters.add("%" + value + "%");
        }
        return this;
    }

    public LotteryTicketQueryBuilder addPositionBasedNumbersSearchCondition(
            Integer num1, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6) {
        Integer[] numbers = {num1, num2, num3, num4, num5, num6};
        
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] == null) {
                continue;
            }

            if (!hasWhereClause) {
                query.append(" WHERE ");
                hasWhereClause = true;
            } else {
                query.append(" AND ");
            }

            query.append("[Number ").append(i + 1).append("] = ?");
            parameters.add(numbers[i]);
        }
        return this;
    }

    public LotteryTicketQueryBuilder addExactMatchCondition(String column, String value) {
        if (value != null && !value.isEmpty()) {
    
            if (column.equals("Company") && value.contains(",")) {
                String[] companies = value.split(",");
                if (companies.length > 0) {
                    if (!hasWhereClause) {
                        query.append(" WHERE ");
                        hasWhereClause = true;
                    } else {
                        query.append(" AND ");
                    }
                    query.append(column).append(" IN (");
                    for (int i = 0; i < companies.length; i++) {
                        if (i > 0) {
                            query.append(", ");
                        }
                        query.append("?");
                        parameters.add(companies[i].trim());
                    }
                    query.append(")");
                }
            } else {
                
                if (!hasWhereClause) {
                    query.append(" WHERE ");
                    hasWhereClause = true;
                } else {
                    query.append(" AND ");
                }
                query.append(column).append(" = ?");
                parameters.add(value);
            }
        }
        return this;
    }

    public LotteryTicketQueryBuilder addDateRangeCondition(String dateColumn, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (!hasWhereClause) {
                query.append(" WHERE ");
                hasWhereClause = true;
            } else {
                query.append(" AND ");
            }
            query.append(dateColumn).append(" >= ? AND ").append(dateColumn).append(" <= ?");
            parameters.add(java.sql.Date.valueOf(startDate));
            parameters.add(java.sql.Date.valueOf(endDate));
        } else if (startDate != null) {
            if (!hasWhereClause) {
                query.append(" WHERE ");
                hasWhereClause = true;
            } else {
                query.append(" AND ");
            }
            query.append(dateColumn).append(" >= ?");
            parameters.add(java.sql.Date.valueOf(startDate));
        } else if (endDate != null) {
            if (!hasWhereClause) {
                query.append(" WHERE ");
                hasWhereClause = true;
            } else {
                query.append(" AND ");
            }
            query.append(dateColumn).append(" <= ?");
            parameters.add(java.sql.Date.valueOf(endDate));
        }
        return this;
    }

    public LotteryTicketQueryBuilder addDateCondition(String dateColumn, LocalDate specificDate) {
        if (specificDate != null) {
            if (!hasWhereClause) {
                query.append(" WHERE ");
                hasWhereClause = true;
            } else {
                query.append(" AND ");
            }
            query.append(dateColumn).append(" = ?");
            parameters.add(java.sql.Date.valueOf(specificDate));
        }
        return this;
    }

    public LotteryTicketQueryBuilder addOrderBy(String column, boolean descending) {
        query.append(" ORDER BY ").append(column);
        if (descending) {
            query.append(" DESC");
        }
        return this;
    }

    public LotteryTicketQueryBuilder addPagination(int offset, int limit) {
        query.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        parameters.add(offset);
        parameters.add(limit);
        return this;
    }

    public String getQuery() {
        return query.toString();
    }

    public Object[] getParameters() {
        return parameters.toArray();
    }

    public int getParameterCount() {
        return parameters.size();
    }
}
