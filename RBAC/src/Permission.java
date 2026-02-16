public record Permission(String name, String resource, String description) {
    public Permission {
        if (name == null) {
            throw new IllegalArgumentException("Название права не может быть null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Ресурс не может быть null");
        }
        if (description == null) {
            throw new IllegalArgumentException("Описание не может быть null");
        }
        String normalizedName = name.trim().toUpperCase();
        if (normalizedName.contains(" ")) {
            throw new IllegalArgumentException(
                    "Название права не должно содержать пробелов. Получено: '" + name + "'"
            );
        }
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Название права не может быть пустым");
        }
        String normalizedResource = resource.trim().toLowerCase();
        if (normalizedResource.isEmpty()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым");
        }
        String normalizedDescription = description.trim();
        if (normalizedDescription.isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        name = normalizedName;
        resource = normalizedResource;
        description = normalizedDescription;
    }
    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }
    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = (namePattern == null || namePattern.isBlank()) ||
                name.contains(namePattern.toUpperCase());
        boolean resourceMatches = (resourcePattern == null || resourcePattern.isBlank()) ||
                resource.contains(resourcePattern.toLowerCase());
        return nameMatches && resourceMatches;
    }
    @Override
    public String toString() {
        return format();
    }
}