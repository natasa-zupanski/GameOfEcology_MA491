import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Main {

    // start empty
    private Map<ArrayList<Dragon>, Location> dragonsAtLocations = new HashMap<>();
    private int days = 0;
    private Constants constants = new Constants();

    public static void main(String[] args) {

    }

    public void run(int days_to_run) {

    }

    protected class Constants {
        protected int initialLlamas = 466667;
        protected double birth_multiplier = 0;
        protected double hibernate_multiplier = 1;
        protected double llama_growth_rate = 0.28;
        protected double birth_energy = 0; // ncluding fire and reproduction
        protected double max_weight_loss_hibernate = 0.2;
        protected int max_hibernate_day = 365 * 1;
    }

    protected class Inputs {

    }

    protected class Dragon {
        double weight = 10; // in kg
        int birth = 0; // in days
        double weight_at_start_hibernate = 10;

        public Dragon(int birth) {
            this.birth = birth;
        }

        public void startHibernate() {
            weight_at_start_hibernate = weight;
        }

        public boolean isDeadAfterHibernate() {
            if (weight < (1 - constants.max_weight_loss_hibernate) * weight_at_start_hibernate) {
                return true;
            } else {
                return false;
            }
        }

        public int getAge() {
            return days - birth;
        }
    }

    protected class Location {
        int llamas = 0;
        boolean hibernate = false;
        int hibernate_day = 0;
    }

    public void runDay() {
        for (ArrayList<Dragon> dragons : dragonsAtLocations.keySet()) {
            Location current_location = dragonsAtLocations.get(dragons);
            runDayFor(dragons, current_location);
            days++;
        }
    }

    public void runDayFor(ArrayList<Dragon> dragons, Location current_location) {
        boolean hibernate = current_location.hibernate;
        final boolean hibernate_for_energy = hibernate;

        ArrayList<Double> energies = (ArrayList<Double>) dragons.stream()
                .map(dragon -> getEnergyForDay(dragon, hibernate_for_energy)).toList();

        double sum_energy = energies.stream().reduce(0.0,
                (Double subtotal, Double element) -> {
                    return subtotal + element;
                });
        int llamas_to_eat = 0;
        if (hibernate) {
            // first check that we can still hibernate
            if (current_location.hibernate_day > constants.max_hibernate_day) {
                current_location.hibernate = false;

                for (Dragon dragon : dragons) {
                    if (dragon.isDeadAfterHibernate()) {
                        dragons.remove(dragon);
                    }
                }

                runDayFor(dragons, current_location);
                return;
            }
        }

        int current_llamas = current_location.llamas;
        if (!hibernate) {
            llamas_to_eat = getLlamasForEnergy(sum_energy);

            if (current_llamas <= constants.hibernate_multiplier * llamas_to_eat) {
                current_location.hibernate = true;
                current_location.hibernate_day = 0;
                runDayFor(dragons, current_location);
                return;
            }

            if (current_llamas > constants.birth_multiplier * llamas_to_eat) { // assume dragons hatch right away
                                                                               // for
                // convenience
                // do birth
                dragonsAtLocations.remove(dragons);
                dragons.add(new Dragon(days));

                // calculate llamas needed to spend energy to birth & bathe in fire
                // adjust llamas to eat
                llamas_to_eat += getLlamasForEnergy(constants.birth_energy);
            }
        }

        // adjust llama population
        int new_llama_population = (int) Math
                .floor(current_llamas - llamas_to_eat + constants.llama_growth_rate *
                        current_llamas);
        current_location.llamas = new_llama_population;

        // adjust dragon weights
        for (int i = 0; i < energies.size(); i++) {
            adjustWeight(dragons.get(i), energies.get(i), hibernate);
        }

        // adjust location
        if (hibernate) {
            current_location.hibernate_day += 1;
        }

        // save changes
        dragonsAtLocations.put(dragons, current_location);
    }

    public void adjustWeight(Dragon dragon, double energy, boolean hibernate) {
        if (hibernate) {
            double prop_for_growth = getProportionEnergyToGrowth(dragon);
            dragon.weight = getNewWeight(dragon, prop_for_growth * energy);
        } else {

        }

    }

    public int getLlamasForEnergy(Double energy) {
        return (int) Math.ceil(energy / 248800);
    }

    public double getNewWeight(Dragon dragon, double energy) {
        return 0;
    }

    public double getProportionEnergyToGrowth(Dragon dragon) {
        return ((46.4 / (1 + Math.exp(-1 / (200 * (dragon.getAge() - 365))))) + 56.4) / 100;
    }

    public double getEnergyForDay(Dragon dragon, boolean hibernate) {
        if (!hibernate) {
            double total_no_growth = 21 * getMetabolicEnergyPerHourStandard(dragon) + 3 * getEnergyForFlying(dragon);
            double prop_for_growth = getProportionEnergyToGrowth(dragon);
            return (1 / (1 - prop_for_growth)) * total_no_growth;
        } else {
            return 0.012 * 24 * getMetabolicEnergyPerHourStandard(dragon);
        }
    }

    public double getMetabolicEnergyPerHourStandard(Dragon dragon) {
        return Math.pow(10, (Math.log10(3.1) + 0.744 * Math.log10(dragon.weight)));
    }

    public double getEnergyForFlying(Dragon dragon) {
        return Math.pow(10, Math.log10(37.152) + 0.744 * Math.log10(dragon.weight));
    }

}