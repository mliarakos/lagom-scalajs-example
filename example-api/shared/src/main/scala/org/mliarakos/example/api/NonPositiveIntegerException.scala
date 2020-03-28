package org.mliarakos.example.api

import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}

case class NonPositiveIntegerException(
    override val errorCode: TransportErrorCode,
    override val exceptionMessage: ExceptionMessage
) extends TransportException(errorCode, exceptionMessage)

object NonPositiveIntegerException {
  val name: String             = classOf[NonPositiveIntegerException].getSimpleName
  val code: TransportErrorCode = TransportErrorCode.BadRequest

  def apply(num: Int): NonPositiveIntegerException =
    NonPositiveIntegerException(code, new ExceptionMessage(name, s"$num is not a positive integer"))
}
