package instep.orm

import instep.orm.sql.*
import net.moznion.random.string.RandomStringGenerator
import org.testng.annotations.Test
import java.time.LocalDateTime
import java.util.*

object TableTest {
    val stringGenerator = RandomStringGenerator()
    val datasource = InstepSQLTest.datasource

    object AccountTable : Table("account_" + stringGenerator.generateByRegex("[a-z]{8}")) {
        val id = autoIncrementLong("id").primary()
        val name = varchar("name", 256).notnull()
        val balance = numeric("balance", Int.MAX_VALUE, 2).notnull()
        val createdAt = datetime("created_at").notnull()
        val avatar = lob("avatar")
    }

    @Test
    fun createAccountTable() {
        AccountTable.create().debug().execute()
    }

    @Test(dependsOnMethods = arrayOf("createAccountTable"), priority = 1)
    fun addColumn() {
        AccountTable.addColumn(AccountTable.boolean("verified").default("FALSE")).debug().execute()
    }

    @Test(dependsOnMethods = arrayOf("createAccountTable"))
    fun insertAccounts() {
        val random = Random()
        val total = random.ints(10, 100).findAny().orElse(100)

        for (index in 0..total) {
            val name = stringGenerator.generateByRegex("\\w{1,256}")
            AccountTable.insert().addValues(
                null,
                name,
                random.nextDouble(),
                LocalDateTime.now(),
                null
            ).execute()
        }

        for (index in 0..total) {
            val name = stringGenerator.generateByRegex("\\w{1,256}")
            AccountTable.insert()
                .addValue(AccountTable.name, name)
                .addValue(AccountTable.balance, random.nextDouble())
                .addValue(AccountTable.createdAt, LocalDateTime.now())
                .debug()
                .execute()
        }
    }

    @Test(dependsOnMethods = arrayOf("insertAccounts"))
    fun maxAccountId() {
        AccountTable.select(AccountTable.id.max()).debug().executeScalar().toInt()
    }

    @Test(dependsOnMethods = arrayOf("maxAccountId"))
    fun updateAccounts() {
        val random = Random()
        val max = AccountTable.select(AccountTable.id.max()).executeScalar().toInt()
        val id = random.ints(1, max).findAny().orElse(max)

        AccountTable.update()
            .set(AccountTable.name, "laozi")
            .set(AccountTable.balance, 3.33)
            .where(id)
            .debug()
            .executeUpdate()

        var laozi = AccountTable[id]!!
        assert(laozi[AccountTable.name] == "laozi")
        assert(laozi[AccountTable.balance] == 3.33)


        AccountTable.update()
            .set(AccountTable.name, "dao de jing")
            .set(AccountTable.balance, 6.66)
            .where(AccountTable.name eq "laozi", AccountTable.balance lte 3.33)
            .debug()
            .executeUpdate()

        laozi = AccountTable.select().where(AccountTable.id eq id).debug().execute().single()
        assert(laozi[AccountTable.name] == "dao de jing")
        assert(laozi[AccountTable.balance] == 6.66)
    }

    @Test(dependsOnMethods = arrayOf("maxAccountId"))
    fun deleteAccounts() {
        val random = Random()
        val max = AccountTable.select(AccountTable.id.max()).executeScalar().toInt()
        val id = random.ints(1, max).findAny().orElse(max)

        AccountTable.delete().where(AccountTable.id eq id).executeUpdate().let { println(it) }
        assert(null == AccountTable[id])
    }
}
