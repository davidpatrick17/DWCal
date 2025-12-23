package Clan420.highlife.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * HighLife GTA RP Mechanic Invoice Helper
 * - Lets you add line items with server-allowed price ranges
 * - Warns if out of range (you can still accept or re-enter)
 * - Prints a neat invoice you can copy into a Google Doc
 */
public class InvoiceCalculator {

    // Define your job types and allowed min/max ranges
    enum JobType {
        ROADSIDE("Roadside", 300, 1000),
        REFUEL("Refuel", 200, 250),
        FLIP("Flip", 100, 500),

        // Future placeholders (edit ranges once you know them)
        COSMETIC("Cosmetic", 0, 0),
        BODYWORK("Body Work", 0, 0),
        PERFORMANCE("Performance", 0, 0);

        final String label;
        final int min;
        final int max;

        JobType(String label, int min, int max) {
            this.label = label;
            this.min = min;
            this.max = max;
        }

        boolean hasRange() {
            return !(min == 0 && max == 0);
        }
    }

    static class LineItem {
        final JobType type;
        final String notes;
        final BigDecimal amount;

        LineItem(JobType type, String notes, BigDecimal amount) {
            this.type = type;
            this.notes = notes;
            this.amount = amount;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== HighLife Mechanic Invoice Helper ===");
        System.out.print("Customer name (or IGN): ");
        String customer = sc.nextLine().trim();

        System.out.print("Vehicle (optional): ");
        String vehicle = sc.nextLine().trim();

        List<LineItem> items = new ArrayList<>();

        while (true) {
            System.out.println("\nAdd a line item:");
            printMenu();

            System.out.print("Choose (number), or 0 to finish: ");
            int choice = readInt(sc);

            if (choice == 0) break;

            JobType type = mapChoice(choice);
            if (type == null) {
                System.out.println("Invalid choice.");
                continue;
            }

            System.out.print("Notes (optional, e.g., 'Highway callout', '2 jerry cans'): ");
            String notes = sc.nextLine().trim();

            BigDecimal amount = readPriceWithRangeCheck(sc, type);

            items.add(new LineItem(type, notes, amount));
            System.out.println("Added: " + type.label + " - $" + amount);
        }

        if (items.isEmpty()) {
            System.out.println("\nNo items added. Exiting.");
            return;
        }

        printInvoice(customer, vehicle, items);
    }

    private static void printMenu() {
        System.out.println("1) Roadside ($300 - $1000)");
        System.out.println("2) Refuel   ($200 - $250)");
        System.out.println("3) Flip     ($100 - $500)");
        System.out.println("4) Cosmetic (set range later)");
        System.out.println("5) Body Work (set range later)");
        System.out.println("6) Performance (set range later)");
    }

    private static JobType mapChoice(int choice) {
        return switch (choice) {
            case 1 -> JobType.ROADSIDE;
            case 2 -> JobType.REFUEL;
            case 3 -> JobType.FLIP;
            case 4 -> JobType.COSMETIC;
            case 5 -> JobType.BODYWORK;
            case 6 -> JobType.PERFORMANCE;
            default -> null;
        };
    }

    private static BigDecimal readPriceWithRangeCheck(Scanner sc, JobType type) {
        while (true) {
            System.out.print("Enter price for " + type.label + " (numbers only): $");
            BigDecimal amount = readMoney(sc);

            if (!type.hasRange()) {
                // No known range yet, accept anything >= 0
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("Price can't be negative.");
                    continue;
                }
                return amount;
            }

            BigDecimal min = BigDecimal.valueOf(type.min);
            BigDecimal max = BigDecimal.valueOf(type.max);

            if (amount.compareTo(min) < 0 || amount.compareTo(max) > 0) {
                System.out.println("⚠ Out of allowed range for " + type.label +
                        " ($" + type.min + " - $" + type.max + ").");

                System.out.print("Type 'y' to accept anyway, or 'n' to re-enter: ");
                String yn = sc.nextLine().trim().toLowerCase(Locale.ROOT);
                if (yn.equals("y")) return amount;
                // else loop again
            } else {
                return amount;
            }
        }
    }

    private static int readInt(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid whole number: ");
            }
        }
    }

    private static BigDecimal readMoney(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim().replace("$", "");
            try {
                BigDecimal val = new BigDecimal(s);
                // Force 2dp just in case you ever use decimals
                return val.setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number (e.g., 250 or 250.00): $");
            }
        }
    }

    private static void printInvoice(String customer, String vehicle, List<LineItem> items) {
        BigDecimal subtotal = items.stream()
                .map(li -> li.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        System.out.println("\n\n===== INVOICE (copy this into Google Docs) =====");
        System.out.println("Date/Time: " + time);
        System.out.println("Customer:  " + (customer.isBlank() ? "-" : customer));
        System.out.println("Vehicle:   " + (vehicle.isBlank() ? "-" : vehicle));
        System.out.println("-----------------------------------------------");

        int i = 1;
        for (LineItem li : items) {
            String notes = li.notes.isBlank() ? "" : " (" + li.notes + ")";
            System.out.printf("%d) %-12s%s  $%s%n", i++, li.type.label, notes, li.amount);
        }

        System.out.println("-----------------------------------------------");
        System.out.println("TOTAL: $" + subtotal.setScale(2, RoundingMode.HALF_UP));
        System.out.println("===============================================");
    }
}
