package com.example.example.sqlite3

import liquibase.Scope
import liquibase.Scope.ScopedRunner
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.resource.{DirectoryResourceAccessor, SearchPathResourceAccessor}
import org.scalatest.Suite

import java.nio.file.Path
import java.sql.{Connection, DriverManager}
import java.util.UUID

trait InMemoryDatabase
{ self: Suite =>
  def withDatabase(test: Connection => Any): Unit = {
    val databaseUrl = s"jdbc:sqlite:file:memory-${UUID.randomUUID().toString}?mode=memory&cache=shared"
    implicit val connection: Connection = DriverManager.getConnection(databaseUrl)

    val resourceAccessor: SearchPathResourceAccessor = new SearchPathResourceAccessor(
      new DirectoryResourceAccessor(Path.of("src/main/resources/liquibase"))
    )
    Scope.child(Scope.Attr.resourceAccessor.name(), resourceAccessor, new ScopedRunner[Unit] {
      override def run(): Unit = {
        val commandScope = new CommandScope("update")
          .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelog.xml")
          .addArgumentValue("url", databaseUrl)
        commandScope.execute()
      }
    })
    test(connection)
    connection.close()
  }
}
