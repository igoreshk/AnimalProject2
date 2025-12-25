package Animal_Demykin.factory;
import Animal_Demykin.animals.Animal;
import Animal_Demykin.animals.Cat;
import Animal_Demykin.animals.Dog;
import Animal_Demykin.animals.Duck;


public class AnimalFactory {

    public static Animal createAnimal(String type, String name, int age, double weight, String color) {
        return switch (type.toLowerCase()) {
            case "cat" -> new Cat(name, age, weight, color);
            case "dog" -> new Dog(name, age, weight, color);
            case "duck" -> new Duck(name, age, weight, color);
            default -> null;
        };
    }
}
