package com.n0n5ense.persistence

fun databaseInit() {
    UserService.init()
    TouchCardService.init()
    TouchLogService.init()
    PhysicalLogService.init()
}