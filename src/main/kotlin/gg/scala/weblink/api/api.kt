package gg.scala.weblink.api

val EMAIL_REGEX =
    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$".toRegex(RegexOption.IGNORE_CASE)

const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"


const val API_URL =
    "http://100.125.41.103:8081/forum/account/sendRegistration/%s"

const val API_TOKEN =
    "Mmvb500e9cZShvMgxnI44XGjyzIXOX5a"