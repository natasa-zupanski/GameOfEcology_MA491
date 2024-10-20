import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Main {

    // start empty
    private Map<ArrayList<Dragon>, Location> dragonsAtLocations = new HashMap<>();
    private int days = 0;
    private Constants constants = new Constants();

    public static void main(String[] args) {

    }

    public void run() {

    }

    protected class Constants {
        protected int initialLlamas = 466667;
        protected double birth_multiplier = 0;
        protected double hibernate_multiplier = 1;
        protected double llama_growth_rate = 0;
        protected double birth_energy = 0; // ncluding fire and reproduction
        protected double max_weight_loss_hibernate = 0.2;
        protected int max_hibernate_day = 365 * 1;
    }

    protected class Inputs {

    }

    protected class Dragon {
        double weight = 10; // in kg
        int birth_age = 0; // in days
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
                runDayFor(dragons, current_location); // TODO: add a check that there is enough energy not to just hibernate again, else die
                // TODO: adjust dragon population based on who died during hibernation.
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
                dragons.add(new Dragon());

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
        for (int i = 0; i < dragons.size(); i++) {
            adjustWeight(dragons.get(i), energies.get(i), hibernate); // TODO: consider how to deal with case of new
                                                                      // born dragon in here, ignore?
        }

        // adjust location
        if (hibernate)

            // save changes
            dragonsAtLocations.put(dragons, current_location);
    }

    public void adjustWeight(Dragon dragon, double energy, boolean hibernate) {

    }

    public void runDayHibernateFor(Set<Dragon> dragons, Location current_location) {

    }

    // public void runDayActiveFor(Set<Dragon> dragons, Location current_location) {
    // double sum_energy = dragons.stream().map(dragon -> getEnergyForDay(dragon))
    // .reduce(0.0,
    // (Double subtotal, Double element) -> {
    // return subtotal + element;
    // });
    // int llamas_to_eat = getLlamasForEnergy(sum_energy);
    // int current_llamas = current_location.llamas;

    // if (current_llamas <= constants.hibernate_multiplier * llamas_to_eat) {
    // // hibernate

    // return;
    // }

    // if (current_llamas > constants.birth_multiplier * llamas_to_eat) { // assume
    // dragons hatch right away for
    // // convenience
    // // do birth
    // dragonsAtLocations.remove(dragons);
    // dragons.add(new Dragon());

    // // calculate llamas needed to spend energy to birth & bathe in fire
    // // adjust llamas to eat
    // llamas_to_eat += getLlamasForEnergy(constants.birth_energy);
    // }

    // int new_llama_population = (int) Math
    // .floor(current_llamas - llamas_to_eat + constants.llama_growth_rate *
    // current_llamas);
    // current_location.llamas = new_llama_population;
    // dragonsAtLocations.put(dragons, current_location);
    // }

    public int getLlamasForEnergy(Double energy) {
        return 0;
    }

    public double getNewWeight(Dragon dragon, double energy) {
        return 0;
    }

    public double getProportionEnergyToGrowth(Dragon dragon) {
        return 0;
    }

    public double getEnergyForDay(Dragon dragon, boolean hibernate) {
        return 0;
    }

    public double getEnergyForFlying(Dragon dragon) {
        return 0;
    }

}