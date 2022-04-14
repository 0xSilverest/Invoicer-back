package dev.silverest.invoicerback.daos

import dev.silverest.invoicerback.models.*

case class CompanyDao (
  name: String,
  email: Email,
  phoneNumber: PhoneNumber,
  address: Address,
  iceNumber: ICENumber
)

object CompanyDao:
  given companyDaoMapper: Mapper[CompanyDao, Client.Company] with
    def modelMapper(c: CompanyDao, userId: String): Client.Company =
      Company(c.name,
              c.email,
              c.phoneNumber,
              c.address,
              c.iceNumber,
              userId)

