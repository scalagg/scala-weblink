package gg.scala.weblink.api

val EMAIL_REGEX =
    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$".toRegex(RegexOption.IGNORE_CASE)

const val AUTHENTICATION_TOKEN =
    "dCP87UegxMsW2uYhJ5B43mzXGbDkLjtVTvRFASQw9EnHZNf"

const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"


const val BASE_API_URL =
    "https://solara.gg/api/v1/%s?auth=$AUTHENTICATION_TOKEN"
