import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Main {

    private static final String CHOSEN_CATEGORY = "Video Games";
    private static final boolean VERBOSE_LOGGING = false;

    public static void main(String[] args) throws Exception {
        long initialBudget = 10_000_000L;

        if (args.length > 0) {
            try {
                initialBudget = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse budget argument, using default.");
            }
        }

        long remainingBudget = initialBudget;
        long totalSpent = 0L;
        int rounds = 0;
        int wins = 0;
        int losses = 0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out, true);
        AuctionInput input = new AuctionInput();

        System.err.println("Bot started with budget: " + initialBudget);
        System.err.println("Category=" + CHOSEN_CATEGORY);

        out.println(CHOSEN_CATEGORY);

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            char firstChar = line.charAt(0);

            if (firstChar == 'W') {
                long spent = parseLongAfterSpace(line);
                remainingBudget -= spent;
                if (remainingBudget < 0L) {
                    remainingBudget = 0L;
                }
                totalSpent += spent;
                wins++;
                if (VERBOSE_LOGGING) {
                    System.err.println("Win. Spent=" + spent + ", remaining=" + remainingBudget);
                }
                continue;
            }

            if (line.equals("L")) {
                losses++;
                continue;
            }

            if (firstChar == 'S' && line.length() > 2) {
                System.err.println("Summary: " + line
                        + " | rounds=" + rounds
                        + " wins=" + wins
                        + " losses=" + losses
                        + " totalSpent=" + totalSpent
                        + " remaining=" + remainingBudget);
                continue;
            }

            if (parseAuctionLine(line, input)) {
                rounds++;

                int score = scoreBid(input);
                int[] bid = chooseBid(score, remainingBudget, initialBudget, totalSpent);

                out.println(bid[0] + " " + bid[1]);
                if (VERBOSE_LOGGING) {
                    System.err.println("Auction score=" + score
                            + ", bid=" + bid[0] + " " + bid[1]
                            + ", remaining=" + remainingBudget);
                }
                continue;
            }

            if (VERBOSE_LOGGING) {
                System.err.println("Unknown line: " + line);
            }
        }

        System.err.println("Input closed, bot exiting.");
    }

    private static int scoreBid(AuctionInput input) {
        int score = 0;

        boolean videoMatch = CHOSEN_CATEGORY.equals(input.videoCategory);
        boolean firstInterestMatch = CHOSEN_CATEGORY.equals(input.interest1);
        boolean secondInterestMatch = CHOSEN_CATEGORY.equals(input.interest2);
        boolean thirdInterestMatch = CHOSEN_CATEGORY.equals(input.interest3);

        if (videoMatch) {
            score += 5;
        }

        if (firstInterestMatch) {
            score += 5;
        } else if (secondInterestMatch) {
            score += 1;
        }

        if (videoMatch && firstInterestMatch) {
            score += 4;
        } else if (videoMatch && (secondInterestMatch || thirdInterestMatch)) {
            score += 1;
        }

        if (input.subscribed) {
            score += 2;
        }

        if (videoMatch && firstInterestMatch && input.engagement >= 0.012) {
            score += 1;
        }

        if (input.engagement >= 0.030) {
            score += 7;
        } else if (input.engagement >= 0.020) {
            score += 5;
        } else if (input.engagement >= 0.012) {
            score += 3;
        } else if (input.engagement >= 0.006) {
            score += 1;
        }

        long viewCount = input.viewCount;
        if (viewCount < 10_000) {
            score += 1;
        } else if (viewCount < 50_000) {
            score += 1;
        } else if (viewCount < 200_000) {
            score += 3;
        } else if (viewCount < 1_000_000) {
            score += 3;
        } else if (viewCount < 5_000_000) {
            score += 4;
        } else if (viewCount < 20_000_000) {
            score += 2;
        } else if (viewCount < 100_000_000) {
            score += 1;
        }

        return score;
    }

    private static int[] chooseBid(int score, long remainingBudget, long initialBudget, long totalSpent) {
        int startBid;
        int maxBid;

        if (score <= 1) {
            startBid = 0;
            maxBid = 0;
        } else if (score <= 3) {
            startBid = 1;
            maxBid = 2;
        } else if (score <= 5) {
            startBid = 1;
            maxBid = 3;
        } else if (score <= 8) {
            startBid = 2;
            maxBid = 6;
        } else if (score <= 11) {
            startBid = 6;
            maxBid = 12;
        } else if (score <= 13) {
            startBid = 9;
            maxBid = 17;
        } else if (score <= 15) {
            startBid = 14;
            maxBid = 23;
        } else if (score <= 17) {
            startBid = 20;
            maxBid = 29;
        } else {
            startBid = 28;
            maxBid = 38;
        }

        double spendRatio = totalSpent / (double) Math.max(1L, initialBudget);

        if (score >= 16) {
            if (spendRatio < 0.10) {
                startBid += 1;
                maxBid += 2;
            } else if (spendRatio < 0.18) {
                startBid += 1;
                maxBid += 1;
            }
        }

        if (maxBid > remainingBudget) {
            maxBid = (int) Math.max(0L, remainingBudget);
        }
        if (startBid > maxBid) {
            startBid = maxBid;
        }

        return new int[]{startBid, maxBid};
    }

    private static boolean parseAuctionLine(String line, AuctionInput input) {
        input.reset();

        int start = 0;
        int length = line.length();

        while (start < length) {
            int comma = line.indexOf(',', start);
            if (comma < 0) {
                comma = length;
            }

            int eq = line.indexOf('=', start);
            if (eq < 0 || eq > comma) {
                return false;
            }

            String key = line.substring(start, eq).trim();
            String value = line.substring(eq + 1, comma).trim();

            switch (key) {
                case "video.category":
                    input.videoCategory = value;
                    break;
                case "video.viewCount":
                    input.viewCount = parseRequiredLong(value);
                    break;
                case "video.commentCount":
                    input.commentCount = parseRequiredLong(value);
                    break;
                case "viewer.subscribed":
                    input.subscribed = "Y".equals(value);
                    break;
                case "viewer.interests":
                    parseInterests(value, input);
                    break;
                case "viewer.age":
                case "viewer.gender":
                    break;
                default:
                    break;
            }

            start = comma + 1;
        }

        if (input.videoCategory == null
                || input.viewCount < 0L
                || input.commentCount < 0L
                || input.interestsParsed == 0) {
            return false;
        }

        input.engagement = input.commentCount / (double) Math.max(1L, input.viewCount);
        return true;
    }

    private static void parseInterests(String value, AuctionInput input) {
        if (value.isEmpty()) {
            return;
        }

        int firstSep = value.indexOf(';');
        if (firstSep < 0) {
            input.interest1 = value.trim();
            input.interestsParsed = 1;
            return;
        }

        input.interest1 = value.substring(0, firstSep).trim();

        int secondSep = value.indexOf(';', firstSep + 1);
        if (secondSep < 0) {
            input.interest2 = value.substring(firstSep + 1).trim();
            input.interestsParsed = 2;
            return;
        }

        input.interest2 = value.substring(firstSep + 1, secondSep).trim();
        input.interest3 = value.substring(secondSep + 1).trim();
        input.interestsParsed = 3;
    }

    private static long parseLongAfterSpace(String line) {
        int space = line.indexOf(' ');
        if (space < 0 || space == line.length() - 1) {
            return 0L;
        }
        return parseLenientLong(line.substring(space + 1));
    }

    private static long parseRequiredLong(String value) {
        if (value == null) {
            return -1L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static long parseLenientLong(String value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static final class AuctionInput {
        String videoCategory;
        long viewCount;
        long commentCount;
        boolean subscribed;
        String interest1 = "";
        String interest2 = "";
        String interest3 = "";
        int interestsParsed;
        double engagement;

        void reset() {
            videoCategory = null;
            viewCount = -1L;
            commentCount = -1L;
            subscribed = false;
            interest1 = "";
            interest2 = "";
            interest3 = "";
            interestsParsed = 0;
            engagement = 0.0;
        }
    }
}
