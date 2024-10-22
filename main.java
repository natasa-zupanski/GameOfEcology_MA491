import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

class Main {

    // start empty
    private Map<ArrayList<Dragon>, Location> dragonsAtLocations = new HashMap<>();
    Map<ArrayList<Dragon>, Location> copy = new HashMap<>();
    private int days = 0;
    private Constants constants = new Constants();
    ArrayList<ArrayList<LocationData>> stats_data = new ArrayList<>();

    public class LocationData {
        final int dragons;
        final int llamas;
        // int oldest_dragon_age;
        // double average_dragon_age;
        // int youngest_dragon_age;

        public LocationData(int dragons, int llamas) {
            this.dragons = dragons;
            this.llamas = llamas;
        }
    }

    public static void main(String[] args) {
        // TODO: add command line
        Main main = new Main();
        main.run(9 * 365 + 250);
    }

    public void run(int days_to_run) {
        init();
        for (int i = 0; i < days_to_run; i++) {
            runDay();
        }
        // printReport();
        printData();
    }

    public void printReport() {
        int i = 0;
        for (ArrayList<Dragon> dragons : dragonsAtLocations.keySet()) {
            System.out.println("---- ----  " + i + "  ---- ----");
            Location loc = dragonsAtLocations.get(dragons);
            System.out.println("Llama population: " + loc.llamas);
            System.out.println("Hibernating: " + loc.hibernate);
            System.out.println("Dragons: " + dragons.size());
            System.out.println("---- ----");
            for (Dragon dragon : dragons) {
                System.out.println(dragon.toString());
                System.out.println("---- ----");
            }
            System.out.println("---- ----  -  ---- ----");
            i++;
        }
    }

    public void printData() {
        String data = "";
        for (int d = 0; d < stats_data.size(); d++) {
            data += "Day " + d + "\n";
            ArrayList<LocationData> locations = stats_data.get(d);
            String dragons = "";
            String llamas = "";
            for (int i = 0; i < locations.size(); i++) {
                dragons += locations.get(i).dragons + ",";
                llamas += locations.get(i).llamas + ",";
            }
            data += dragons + "\n";
            data += llamas + "\n";
            data += "\n";
            // System.out.println(dragons);
            // System.out.println(llamas);
            // System.out.println("");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        ArrayList<Dragon> init_dragons = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            init_dragons.add(new Dragon(days));
        }
        dragonsAtLocations.put(init_dragons, new Location());
        copy = copy(dragonsAtLocations);
    }

    protected class Constants { // TODO: make sure these are right
        protected int initialLlamas = 583333;
        protected double birth_multiplier = 5;
        protected double hibernate_multiplier = 1;
        protected double llama_growth_rate = 0.28;
        protected double birth_energy = 503.5; // in kJ
        protected double max_weight_loss_hibernate = 0.2;
        protected int max_hibernate_day = 365 * 1;
        protected int min_for_birth = 365 * 5;
        protected double max_llamas_per_acre = 0.0271834219;
        protected double dragon_territory_area = 69867400.6; // in acres
        protected int max_llama_population = (int) Math.floor(0.5 * 69867400.6 * 0.0271834219);
        protected int min_between_birth = 2 * 365;
        protected int min_migrate = 7 * 365;
    }

    protected class Dragon {
        double weight = 10; // in kg
        int birth = 0; // in days
        double weight_at_start_hibernate = -1;
        int last_birth = 0;

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

        public boolean canBirth() {
            return (days - last_birth) > constants.min_between_birth;
        }

        public String toString() {
            return "weight: " + weight + ", age: " + getAge() + ".";
        }

        public Dragon clone() {
            Dragon newDragon = new Dragon(birth);
            newDragon.last_birth = last_birth;
            newDragon.weight = weight;
            newDragon.weight_at_start_hibernate = weight_at_start_hibernate;
            return newDragon;
        }
    }

    protected class Location {
        int llamas = constants.initialLlamas;
        boolean hibernate = false;
        int hibernate_day = 0;

        public Location clone() {
            Location newLocation = new Location();
            newLocation.llamas = llamas;
            newLocation.hibernate = hibernate;
            newLocation.hibernate_day = hibernate_day;
            return newLocation;
        }
    }

    public void runDay() {
        ArrayList<LocationData> data_for_day = new ArrayList<>();
        copy = copy(dragonsAtLocations);
        dragonsAtLocations.clear();
        for (ArrayList<Dragon> dragons : copy.keySet()) {
            Location current_location = copy.get(dragons);
            runDayFor(dragons, current_location, data_for_day);
            // System.out.println("End day: " + days);
            // System.out.println(dragons.get(0));
            days++;
        }
        stats_data.add(data_for_day);
        copy.clear();
    }

    public Map<ArrayList<Dragon>, Location> copy(Map<ArrayList<Dragon>, Location> from) {
        Map<ArrayList<Dragon>, Location> copy = new HashMap<>();
        for (ArrayList<Dragon> dragons : dragonsAtLocations.keySet()) {
            Location current_location = from.get(dragons);
            ArrayList<Dragon> copy_dragons = new ArrayList<>();
            for (Dragon dragon : dragons) {
                copy_dragons.add((Dragon) dragon.clone());
            }
            copy.put(copy_dragons, (Location) current_location.clone());
        }
        return copy;
    }

    public void runDayFor(ArrayList<Dragon> dragons, Location current_location,
            ArrayList<LocationData> data) {
        boolean hibernate = current_location.hibernate;
        final boolean hibernate_for_energy = hibernate;

        ArrayList<Double> energies = new ArrayList<>(dragons.stream()
                .map(dragon -> getEnergyForDay(dragon, hibernate_for_energy)).toList());

        double sum_energy = energies.stream().reduce(0.0,
                (Double subtotal, Double element) -> {
                    return subtotal + element;
                });
        int llamas_to_eat = 0;
        if (hibernate) {
            // first check that we can still hibernate
            if (current_location.hibernate_day > constants.max_hibernate_day) {
                current_location.hibernate = false;

                ArrayList<Dragon> toRemove = new ArrayList<>();
                for (Dragon dragon : dragons) {
                    if (dragon.isDeadAfterHibernate()) {
                        toRemove.add(dragon);
                    }
                }

                dragons.removeAll(toRemove);

                runDayFor(dragons, current_location, data);
                return;
            }
        }

        int current_llamas = current_location.llamas;
        if (!hibernate) {
            llamas_to_eat = getLlamasForEnergy(sum_energy);

            if (current_llamas <= constants.hibernate_multiplier * llamas_to_eat) {
                current_location.hibernate = true;
                current_location.hibernate_day = 0;
                for (Dragon dragon : dragons) {
                    if (dragon.weight_at_start_hibernate == -1) {
                        dragon.weight_at_start_hibernate = dragon.weight;
                    }
                }
                runDayFor(dragons, current_location, data);
                return;
            } else {
                for (Dragon dragon : dragons) {
                    dragon.weight_at_start_hibernate = -1;
                }
            }

        }

        // adjust dragon weights
        for (int i = 0; i < energies.size(); i++) {
            adjustWeight(dragons.get(i), energies.get(i), hibernate);
        }

        if (!hibernate) { // consider births and those moving out
            if (current_llamas > constants.birth_multiplier * llamas_to_eat) {
                Dragon birthing = existsBirthingDragon(dragons);
                if (birthing != null) {
                    birthing.last_birth = days;
                    dragonsAtLocations.remove(dragons);
                    dragons.add(new Dragon(days));

                    // calculate llamas needed to spend energy to birth & bathe in fire
                    // adjust llamas to eat
                    llamas_to_eat += getLlamasForEnergy(constants.birth_energy);
                }
            }

            int start_index = getFirstDragonOfAge(dragons);
            if (start_index != -1) {
                dragonsAtLocations.remove(dragons);
                ArrayList<Dragon> toRemove = new ArrayList<>();
                for (int i = start_index + 1; i < dragons.size(); i++) {
                    // ensure one adult dragon left behind
                    Dragon dragon = dragons.get(i);

                    if (dragon.getAge() >= constants.min_migrate) {
                        // System.out.println(i);
                        toRemove.add(dragon);
                        ArrayList<Dragon> new_dragons = new ArrayList<>();
                        new_dragons.add(dragon);
                        dragonsAtLocations.put(new_dragons, new Location());
                    }
                }

                dragons.removeAll(toRemove);
            }
        }

        // adjust llama population
        int new_llama_population = (int) Math
                .floor(current_llamas - llamas_to_eat + constants.llama_growth_rate *
                        current_llamas);

        new_llama_population = Math.min(new_llama_population, constants.max_llama_population);
        current_location.llamas = new_llama_population;

        // adjust location
        if (hibernate) {
            current_location.hibernate_day += 1;
        }

        // record data.
        data.add(new LocationData(dragons.size(), new_llama_population));

        // save changes
        dragonsAtLocations.put(dragons, current_location);
    }

    public int getFirstDragonOfAge(ArrayList<Dragon> dragons) {
        for (int i = 0; i < dragons.size(); i++) {
            Dragon dragon = dragons.get(i);
            if (dragon.getAge() >= constants.min_migrate) {
                return i;
            }
        }
        return -1;
    }

    public Dragon existsBirthingDragon(ArrayList<Dragon> dragons) {
        for (Dragon dragon : dragons) {
            if (dragon.getAge() >= constants.min_for_birth && dragon.canBirth()) {
                return dragon;
            }
        }
        return null;
    }

    public void adjustWeight(Dragon dragon, double energy, boolean hibernate) {
        if (hibernate) {
            dragon.weight = getNewWeight(dragon, -1 * energy);
        } else {
            double prop_for_growth = getProportionEnergyToGrowth(dragon);
            dragon.weight = getNewWeight(dragon, prop_for_growth * energy);
        }

    }

    public int getLlamasForEnergy(Double energy) {
        return (int) Math.ceil(energy / 248800);
    }

    public double getNewWeight(Dragon dragon, double energy) {
        double change_weight = 0.0000548 * energy;
        return dragon.weight + change_weight;
    }

    public double getProportionEnergyToGrowth(Dragon dragon) {
        return ((46.4 / (1 + Math.exp((-1 / 200) * (dragon.getAge() - 365)))) + 56.4) / 100;
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