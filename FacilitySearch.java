import java.util.ArrayList;
import java.util.Random;

public class FacilitySearch extends SimulatedAnnealing {

    public FacilitySearch(Customer[] customers, Facility[] facilities) {
        this.customers = customers;
        this.facilities = facilities;
    }

    Customer[] customers;
    Facility[] facilities;

    int[] used_caps;
    int[] assignment;
    ArrayList<Integer>[] c_in_f;

    class ReassignCustomer extends ChangeTemperature {

        public ReassignCustomer(int facility, int customer) {
            this.facility = facility;
            this.customer = customer;
        }

        int facility;
        int customer;

        protected void execute() {
            used_caps[assignment[customer]] -= customers[customer].demand;
            used_caps[facility] += customers[customer].demand;
            c_in_f[assignment[customer]].remove(new Integer(customer));
            assignment[customer] = facility;
            c_in_f[facility].add(customer);
        }

        protected boolean acceptFunction(double temperature, double change) {
            if (Math.exp(-change / temperature) > 0.5) {
                return true;
            } else
                return false;
        }

        public double calcNetChange() {
            double reduction = (customers[customer].demand == used_caps[assignment[customer]])
                    ? facilities[assignment[customer]].cost + dist(customer, assignment[customer])
                    : dist(customer, assignment[customer]);
            double increase = (used_caps[facility] == 0) ? facilities[facility].cost + dist(customer, facility)
                    : dist(customer, facility);
            return increase - reduction;
        }

        public boolean allowed() {
            if (customers[customer].demand > facilities[facility].capacity - used_caps[facility]) {
                return false;
            } else
                return true;
        }

    }

    double dist(int c, int f) {
        double x_dist = customers[c].x - facilities[f].x;
        double y_dist = customers[c].y - facilities[f].y;
        return Math.sqrt(x_dist * x_dist + y_dist * y_dist);
    }

    class ChangeTemperaturePriorityQueue {

        public ChangeTemperaturePriorityQueue(int num) {
            heap = new ChangeTemperature[num];
        }

        void insert(ChangeTemperature lm) {
            heap[firstspace] = lm;
            int position = firstspace;
            num_spaces--;
            System.arraycopy(spaces, 1, spaces, 0, num_spaces);
            spaces[num_spaces] = 0;

            // when position is 0, (position-1)/2 = 0, and therefore the net
            // values are equal, therefore loop breaks when top is reached
            while (heap[(position - 1) / 2].calcNetChange() < heap[position].calcNetChange()) {
                ChangeTemperature swap = heap[(position - 1) / 2];
                heap[(position - 1) / 2] = heap[position];
                heap[position] = swap;
                position = (position - 1) / 2;
            }

        }

        ChangeTemperature remove(int index) {
            ChangeTemperature toReturn = heap[index];
            while (index * 2 + 1 < heap.length) {
                if (index * 2 + 2 == heap.length
                        || heap[index * 2 + 1].calcNetChange() > heap[index * 2 + 2].calcNetChange()) {
                    heap[index] = heap[index * 2 + 1];
                    heap[index * 2 + 1] = null;
                    index = index * 2 + 1;
                } else {
                    heap[index] = heap[index * 2 + 2];
                    heap[index * 2 + 2] = null;
                    index = index * 2 + 2;
                }
            }

            int i = -1;
            while (spaces[++i] < index)
                ;
            System.arraycopy(spaces, i, spaces, i + 1, num_spaces);
            spaces[i] = index;
            num_spaces++;
            if (i == 0)
                firstspace = index;

            return toReturn;
        }

        ChangeTemperature[] heap;
        
        int[] spaces;
        int firstspace;
        int num_spaces;

    }

    protected boolean endTempChange() {
        if (super.getSteps() > 250 * (customers.length * facilities.length + 5000)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected double tempFunction(int steps) {
        return FacilityLocations.area * Math.exp(-steps / (double) (20 * customers.length * facilities.length));
    }

    @Override
    protected ChangeTemperature newSelection() {
        Random random = new Random();
        int c = random.nextInt(customers.length);
        int f = random.nextInt(facilities.length);
        int max_viol_f = 0;
        boolean any_violations = false;
        for (int i = 0; i < facilities.length; i++) {
            if (facilities[i].capacity < used_caps[i]
                    && facilities[i].capacity - used_caps[i] <= facilities[max_viol_f].capacity - used_caps[max_viol_f]) {
                max_viol_f = i;
                any_violations = true;
            }
        }
        if (any_violations) {
            c = c_in_f[max_viol_f].get(random.nextInt(c_in_f[max_viol_f].size()));
        }
        return new ReassignCustomer(f, c);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void genInitState() {
        used_caps = new int[facilities.length];
        assignment = new int[customers.length];
        int[] facil_order = FacilityLocations.mapRandomElements(facilities.length);
        int x = 0;
        c_in_f = new ArrayList[facilities.length];
        for (int i = 0; i < facilities.length; i++) {
            c_in_f[i] = new ArrayList<Integer>(customers.length / facilities.length + 1);
        }
        for (int i = 0; i < customers.length; i++) {
            while (facilities[facil_order[x]].capacity
                    - used_caps[facil_order[x]] < customers[FacilityLocations.c_o_demand[i]].demand) {
                x++;

            }
            assignment[FacilityLocations.c_o_demand[i]] = facil_order[x];
            used_caps[facil_order[x]] += customers[FacilityLocations.c_o_demand[i]].demand;
            c_in_f[facil_order[x]].add(i);
        }
    }

    public double customerCost() {
        ArrayList<Integer> open_facilities = new ArrayList<Integer>(facilities.length);
        double sum = 0;
        for (int i = 0; i < customers.length; i++) {
            if (!open_facilities.contains(assignment[i])) {
                open_facilities.add(assignment[i]);
            }
            sum += dist(i, assignment[i]);
        }
        for (Integer i : open_facilities) {
            sum += facilities[i].cost;
        }
        return sum;
    }

    public boolean isLegalState() {
        for (int i = 0; i < facilities.length; i++) {
            if (facilities[i].capacity < used_caps[i]) {
                return false;
            }
        }
        return true;
    }

}
