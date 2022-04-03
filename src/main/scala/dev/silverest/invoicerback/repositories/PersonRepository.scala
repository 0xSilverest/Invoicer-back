package dev.silverest.invoicerback.repositories

import dev.silverest.invoicerback.models.Client.{Person}
import io.getquill.{query, quote}
import zio.{UIO, ZIO}
import io.getquill.*
import H2Context.lift

class PersonRepository