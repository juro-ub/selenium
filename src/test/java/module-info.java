open module test.de.jro.moduls.utils {
    requires de.jro.moduls.selenium;
    requires transitive org.junit.jupiter.engine;
    requires transitive org.junit.jupiter.api;
    
    exports test.de.jro.moduls.selenium;
}
