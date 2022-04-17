package dev.silverest.invoicerback.daos

import dev.silverest.invoicerback.models.*

case class CompanyDao (
  id: Option[Int],
  name: String,
  email: Email,
  phoneNumber: PhoneNumber,
  address: Address,
  iceNumber: ICENumber
)

object CompanyDao:
  given companyDaoMapper: Mapper[CompanyDao, Client.Company] with
    def modelMapper(c: CompanyDao, userId: String): Client.Company =
      Company(c.id.getOrElse(-1), c.name, c.email, c.phoneNumber,
              c.address, c.iceNumber, userId)

  given companyUniques: Unique[CompanyDao, ICENumber] with
    def uniquesExtractor(c: CompanyDao): ICENumber=
      c.iceNumber

  given companyId: Unique[CompanyDao, Int] with
    def uniquesExtractor(c: CompanyDao): Int =
      c.id.getOrElse(-1)
