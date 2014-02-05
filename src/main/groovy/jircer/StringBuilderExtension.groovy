package jircer

class StringBuilderExtension {

    /**
     * @see String#isEmpty()
     */
    static boolean isEmpty(StringBuilder self) {
        return self.length() == 0
    }

    /**
     * Removes all characters from the builder.  The capacity is left untouched.
     *
     * @see StringBuilder#trimToSize()
     */
    static void clear(StringBuilder self) {
        self.length = 0
    }

    /**
     * @see String#startsWith(String)
     */
    static boolean startsWith(StringBuilder self, String prefix) {
        return startsWith(self, prefix, 0)
    }

    /**
     * @see String#startsWith(String, int)
     */
    static boolean startsWith(StringBuilder self, String prefix, int toffset) {
        def ta = self.value
        def to = toffset
        def pa = prefix.value
        int po = 0
        int pc = pa.length
        if ((toffset < 0) || (toffset > self.length() - pc)) {
            return false
        }
        while (--pc >= 0) {
            if (ta[to++] != pa[po++]) {
                return false
            }
        }
        return true
    }

}
