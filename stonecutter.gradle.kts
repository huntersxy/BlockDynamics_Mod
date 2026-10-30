plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.148" apply false
}

stonecutter active "1.21.1-neoforge"

stonecutter handlers {
    inherit("java", "json")
}
