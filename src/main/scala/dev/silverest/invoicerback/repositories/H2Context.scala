package dev.silverest.invoicerback.repositories

import io.getquill._

object H2Context extends H2ZioJdbcContext(SnakeCase)