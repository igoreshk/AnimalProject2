package Animal_Demykin;

import animals.Animal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

enum Command {
    ADD("add"),
    LIST("list"),
    EDIT("edit"),
    FILTER("filter"),
    EXIT("exit");

    private final String command;

    Command(String command) {
        this.command = command;
    }

    public static Command fromString(String text) {
        for (Command cmd : Command.values()) {
            if (cmd.command.equalsIgnoreCase(text.trim())) {
                return cmd;
            }
        }
        return null;
    }
}

public class Main {
    private static List<Animal> animals = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Добро пожаловать в зоопарк с БД MySQL!");

        try {
            // Загружаем животных из БД при старте
            animals = AnimalRepository.getAllAnimals();
            System.out.println("Загружено " + animals.size() + " животных из базы данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка загрузки данных из БД: " + e.getMessage());
        }

        showMenu();
    }

    private static void showMenu() {
        while (true) {
            System.out.println("\n=== Главное меню ===");
            System.out.println("Доступные команды: add, list, edit, filter, exit");
            System.out.print("Введите команду: ");

            String input = scanner.nextLine();
            Command command = Command.fromString(input);

            if (command == null) {
                System.out.println("Неизвестная команда. Попробуйте снова.");
                continue;
            }

            switch (command) {
                case ADD:
                    addAnimal();
                    break;
                case LIST:
                    listAnimals();
                    break;
                case EDIT:
                    editAnimal();
                    break;
                case FILTER:
                    filterAnimals();
                    break;
                case EXIT:
                    System.out.println("Выход из программы. До свидания!");
                    DatabaseConnection.closeConnection();
                    scanner.close();
                    return;
            }
        }
    }

    private static void addAnimal() {
        System.out.println("\n=== Добавление животного ===");
        System.out.println("Выберите тип животного: cat, dog, duck");

        String type = getValidAnimalType();
        System.out.print("Имя: ");
        String name = scanner.nextLine().trim();

        int age = getValidAge();
        double weight = getValidWeight();

        System.out.print("Цвет: ");
        String color = scanner.nextLine().trim();

        Animal animal = AnimalFactory.createAnimal(type, name, age, weight, color);

        if (animal != null) {
            try {
                // Сохраняем в БД
                AnimalRepository.saveAnimal(animal, type);

                // Обновляем локальный список
                animals = AnimalRepository.getAllAnimals();

                System.out.print("Животное добавлено в БД! Оно говорит: ");
                animal.say();

                if (animal instanceof animals.Duck) {
                    animals.Duck duck = (animals.Duck) animal;
                    System.out.print("А ещё умеет летать: ");
                    duck.fly();
                }
            } catch (SQLException e) {
                System.out.println("Ошибка сохранения в БД: " + e.getMessage());
            }
        }
    }

    private static void listAnimals() {
        try {
            // Получаем свежие данные из БД
            animals = AnimalRepository.getAllAnimals();

            if (animals.isEmpty()) {
                System.out.println("Список животных пуст.");
                return;
            }

            System.out.println("\n=== Список всех животных ===");
            System.out.println("Всего животных в БД: " + animals.size());

            for (int i = 0; i < animals.size(); i++) {
                Animal animal = animals.get(i);
                System.out.println((i + 1) + ". " + animal.toString());
            }
        } catch (SQLException e) {
            System.out.println("Ошибка загрузки данных из БД: " + e.getMessage());
        }
    }

    private static void editAnimal() {
        if (animals.isEmpty()) {
            System.out.println("Список животных пуст. Нечего редактировать.");
            return;
        }

        System.out.println("\n=== Редактирование животного ===");

        // Показываем список для выбора
        for (int i = 0; i < animals.size(); i++) {
            System.out.println((i + 1) + ". " + animals.get(i).toString());
        }

        System.out.print("Введите номер животного для редактирования: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (index < 0 || index >= animals.size()) {
                System.out.println("Неверный номер.");
                return;
            }

            Animal oldAnimal = animals.get(index);
            System.out.println("Редактируем: " + oldAnimal.toString());

            // Запрашиваем новые данные
            System.out.print("Новое имя (оставьте пустым для сохранения текущего): ");
            String newName = scanner.nextLine().trim();
            if (newName.isEmpty()) {
                newName = oldAnimal.getName();
            }

            System.out.print("Новый возраст (оставьте пустым для сохранения текущего): ");
            String ageInput = scanner.nextLine().trim();
            int newAge = ageInput.isEmpty() ? oldAnimal.getAge() : Integer.parseInt(ageInput);

            System.out.print("Новый вес (оставьте пустым для сохранения текущего): ");
            String weightInput = scanner.nextLine().trim();
            double newWeight = weightInput.isEmpty() ? oldAnimal.getWeight() : Double.parseDouble(weightInput);

            System.out.print("Новый цвет (оставьте пустым для сохранения текущего): ");
            String newColor = scanner.nextLine().trim();
            if (newColor.isEmpty()) {
                newColor = oldAnimal.getColor();
            }

            // Обновляем в БД
            try {
                // Здесь нужен ID животного - в реальном приложении нужно хранить ID
                // Для простоты обновляем все данные
                AnimalRepository.updateAnimal(index + 1, newName, newAge, newWeight, newColor);

                // Обновляем локальный список
                animals = AnimalRepository.getAllAnimals();

                System.out.println("Животное успешно обновлено в БД!");
            } catch (SQLException e) {
                System.out.println("Ошибка обновления в БД: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("Неверный формат номера.");
        }
    }

    private static void filterAnimals() {
        System.out.println("\n=== Фильтрация животных по типу ===");
        System.out.println("Выберите тип для фильтрации: cat, dog, duck");

        String filterType = getValidAnimalType();

        try {
            List<Animal> filteredAnimals = AnimalRepository.getAnimalsByType(filterType);

            if (filteredAnimals.isEmpty()) {
                System.out.println("Животных типа '" + filterType + "' не найдено.");
                return;
            }

            System.out.println("\n=== Найдено животных типа '" + filterType + "': " + filteredAnimals.size() + " ===");

            for (int i = 0; i < filteredAnimals.size(); i++) {
                Animal animal = filteredAnimals.get(i);
                System.out.println((i + 1) + ". " + animal.toString());
            }
        } catch (SQLException e) {
            System.out.println("Ошибка фильтрации в БД: " + e.getMessage());
        }
    }

    private static String getValidAnimalType() {
        while (true) {
            System.out.print("Тип: ");
            String type = scanner.nextLine().trim().toLowerCase();

            if (type.equals("cat") || type.equals("dog") || type.equals("duck")) {
                return type;
            }
            System.out.println("Ошибка: введите cat, dog или duck.");
        }
    }

    private static int getValidAge() {
        while (true) {
            System.out.print("Возраст (лет): ");
            String input = scanner.nextLine().trim();
            try {
                int age = Integer.parseInt(input);
                if (age >= 0) {
                    return age;
                }
                System.out.println("Ошибка: возраст не может быть отрицательным.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    private static double getValidWeight() {
        while (true) {
            System.out.print("Вес (кг): ");
            String input = scanner.nextLine().trim().replace(',', '.');
            try {
                double weight = Double.parseDouble(input);
                if (weight > 0) {
                    return weight;
                }
                System.out.println("Ошибка: вес должен быть положительным.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число (можно с десятичной точкой).");
            }
        }
    }
}
