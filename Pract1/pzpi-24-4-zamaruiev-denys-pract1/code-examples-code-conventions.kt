// РЕКОМЕНДАЦІЯ 1
// Погано
val d = 5
// Гарно
val daysUntilExpiry = 5

// РЕКОМЕНДАЦІЯ 2
// Погано: змінний стан
var user = getUser()
// Гарно: незмінне посилання
val user1 = getUser()

// РЕКОМЕНДАЦІЯ 3
// Погано: ризик крашу
val name1 = user!!.name
// Гарно: безпечний виклик
val name2 = user?.name ?: "Anon"

// РЕКОМЕНДАЦІЯ 4
val text = "  hello kotlin  "

// Погано: класи-утиліти
val formatted1 = StringUtils.format(text)

// Гарно: extension-функція
val formatted2 = text.formatCustom()

// РЕКОМЕНДАЦІЯ 5
val status = 0

// Погано
if (status == 1) {}

// Гарно
val ACTIVE = 1
if (status == ACTIVE) {}


// ПРИКЛАД
// Поганий приклад
fun process(u: User?){
    var status = u!!.getStatus()
    if (status == 1){
        println(u.getName() + " is active")
    }
}

// Після рефакторингу
const val STATUS_ACTIVE = 1

fun processUser(user: User?){
    val status = user?.status ?: return
    if (status == STATUS_ACTIVE){
        println("${user.name} is active")
    }
}
