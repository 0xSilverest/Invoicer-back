package dev.silverest.invoicerback.repositories

import io.getquill._

object PostgresContext extends PostgresZioJdbcContext(SnakeCase)
