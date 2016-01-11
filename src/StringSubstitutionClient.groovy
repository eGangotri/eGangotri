/**
 * Created by user on 11/19/15.
 * No Packaging is deliberate
 */
class StringSubstitutionClient {


    StringSubstitution createStringSubstitution(Map mapOfArguments = null) {
        StringSubstitution stringSubstitution
        String prefix1 = mapOfArguments?.prefix1 ?: '$'
        String prefix2 = mapOfArguments?.prefix2 ?: '{'
        String suffix = mapOfArguments?.suffix ?: '}'
        Map values = mapOfArguments?.values ?: [name: "Bob", greeting: "Welcome!"]
        String input = mapOfArguments?.input ?: 'Hello ${name}. ${greeting} It\'s nice to meet you, ${name}. ${xyz}'
        String notFoundText = mapOfArguments?.notFoundText ?: "not found"

        stringSubstitution = new StringSubstitution(prefix1: prefix1, prefix2: prefix2, suffix: suffix, values: values, input: input, notFoundText: notFoundText)
        return stringSubstitution
    }

    static main(args) {
        StringSubstitution stringSubstitution = new StringSubstitutionClient().createStringSubstitution()
        println stringSubstitution.parse()
    }

    class StringSubstitution {
        String prefix1
        String prefix2
        String suffix
        Map values
        String input
        String notFoundText


        def parse() {
            def pattern = /\${prefix1}\${prefix2}(\w+)\${suffix}/ // equals /\$\{(\w+)\}/
            String replacedText = input.replaceAll(pattern) { fullMatch, word ->
                return values[word] ?: "### $word $notFoundText ###"
            }
            return replacedText
        }
    }
}
