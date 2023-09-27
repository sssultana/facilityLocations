import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class FacilityLocations {

    static Facility[] facilities;
    static Customer[] customers;
    static int[] c_o_demand;

    static double area;
    
    static double max_x = Double.NEGATIVE_INFINITY;
    static double max_y = Double.NEGATIVE_INFINITY;
    static double min_x = Double.POSITIVE_INFINITY;
    static double min_y = Double.POSITIVE_INFINITY;

    public static void main(String[] args) throws IOException {

        Scanner sc = null;
        String fileName="";
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter filename: ");
        fileName = scan.nextLine();

        try {
            sc = new Scanner(new FileInputStream(new File(fileName)));
        } catch (FileNotFoundException e) {

        }
        String[] details = sc.nextLine().split(" ");
        facilities = new Facility[Integer.parseInt(details[0])];
        customers = new Customer[Integer.parseInt(details[1])];
        for (int i = 0; i < facilities.length; i++) {
            details = sc.nextLine().split(" ");
            facilities[i] = new Facility(Double.parseDouble(details[0]), Integer.parseInt(details[1]), Double.parseDouble(details[2]), Double.parseDouble(details[3]));
            if (facilities[i].x > max_x)
                max_x = facilities[i].x;
            if (facilities[i].y > max_y)
                max_y = facilities[i].y;
            if (facilities[i].x < min_x)
                min_x = facilities[i].x;
            if (facilities[i].y < max_y)
                min_y = facilities[i].y;
        }
        for (int i = 0; i < customers.length; i++) {
            details = sc.nextLine().split(" ");
            customers[i] = new Customer(Integer.parseInt(details[0]), Double.parseDouble(details[1]), Double.parseDouble(details[2]));
            if (customers[i].x > max_x)
                max_x = customers[i].x;
            if (customers[i].y > max_y)
                max_y = customers[i].y;
            if (customers[i].x < min_x)
                min_x = customers[i].x;
            if (customers[i].y < max_y)
                min_y = customers[i].y;
        }

        area = Math.sqrt((max_y - min_y) * (max_y - min_y) + (max_x - min_x) * (max_x - min_x));

        int[] order = mapElements(customers.length);
        for (int i = 0; i < customers.length; i++) {
            int next = i;
            for (int j = i + 1; j < customers.length; j++)
                if (customers[order[j]].demand < customers[order[next]].demand)
                    next = j;
            int swap = order[next];
            order[next] = order[i];
            order[i] = swap;
        }
        c_o_demand = order;
        sc.close();

        FacilitySearch bestPath = null;
        long initialTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - initialTime < 600 * 400) {
            FacilitySearch solver = new FacilitySearch(customers, facilities);
            solver.solve();
            if (bestPath == null) {
                bestPath = solver;
            }
            if (solver.customerCost() < bestPath.customerCost()) {
                bestPath = solver;
            }
        }

        FileWriter fileWriter = new FileWriter("output.txt");
        PrintWriter printItem = new PrintWriter(fileWriter);


        printItem.println(bestPath.customerCost());
        for (int i = 0; i < bestPath.assignment.length; i++) {
            printItem.print(bestPath.assignment[i] + " ");
        }

        printItem.close();

        }

    // helper functions that map elements from a set/array (using the mathematical permutation)

    static int[] mapRandomElements(int len) {
        int[] out = mapElements(len);
        Random random = new Random();
        for (int i = len - 1; i > 0; i--) {
            int n = random.nextInt(i + 1);
            int swap = out[i];
            out[i] = out[n];
            out[n] = swap;
        }
        return out;
    }


    static int[] mapElements(int len) {
        int[] out = new int[len];
        for (int i = 0; i < len; i++) {
            out[i] = i;
        }
        return out;
    }


}