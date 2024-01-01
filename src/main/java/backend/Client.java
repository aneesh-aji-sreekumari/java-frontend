package backend;

public class Client {
    private static boolean matchesPattern(String input, String patternToMatch) {
        // Escape any special characters in the pattern
        return input.matches(patternToMatch);
    }

//     if((N >= 4) && ((matchesPattern(s.substring(N-2, N), " \\d{1}")
//            || matchesPattern(s.substring(N-3, N), " \\d{2}") || matchesPattern(s.substring(N-4, N), " \\d{4}"))))
//            return false;
//        if(N>=5 && matchesPattern(s.substring(N-2, N), " \\d{4}"))
    public static void main(String[] args) {
        System.out.println( matchesPattern(" 89", " \\d{3}"));
    }
}
