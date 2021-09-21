# Boîte à outils programmation fonctionnelle

## Méthodes utilitaires

### relatives aux tableaux d'objets

- Récupération du 1er élément, en gérant la nullité éventuelle du conteneur ou de l'élément
- Obtention d'un `Stream` sans risque de `NullPointer` (`null` produira un flux vide)

### relatives à `java.util.Collection`

- Récupération du 1er élément, en gérant la nullité éventuelle du conteneur ou de l'élément
- Obtention d'un `Stream` sans risque de `NullPointer` (`null` produira un flux vide)
- Obtention d'un `Stream` concaténé à partir de plusieurs instances sans risque de `NullPointer` (`null` produira un flux vide)
- Sélection simple d'éléments et stockage dans une `List`
- Transformation simple de chaque élément et stockage dans une `Collection` au choix ou même dans le produit d'un `Collector` spécifique
- Groupement de valeurs déduites d'éléments, classée par clef également déduite des mêmes éléments
- Fusion de deux instances quelquonques (mais pas `null`) dans une troisième instance, construite pour l'occasion

### relatives à `java.util.function.*`

- Cache d'une valeur renvoyée par un `Supplier` pour une durée limitée, cf. [memoization](https://en.wikipedia.org/wiki/Memoization)
- Composition d'un `Supplier` avec une `Function` (appliquée sur le produit de sa méthode `get()`)

## Conteneurs spécialisés

### `Boundaries`

Minimun *et* maximum, selon un comparateur.

Permet de déterminer les valeurs extrêmes en une seule passe sur les données, en respectant les contraintes de `java.util.stream.Stream#reduce` et en limitant le nombre d'instances crées.

Dispose d'une syntaxe « allégée » pour les objets à trier sur leur ordre naturel ainsi que pour les déterminations à partir d'un `Stream`.

Dispose également d'un `Collector` dédié si les gens préfèrent.

### `IntBoundaries`

Spécialiastion du précédent : entiers minimun *et* maximum, selon l'ordre naturel.

### `Either`

Union disjointe : résultat « nominal », ou valeur représentant l'échec.

Permet de se passer d'exception métier en exprimant les deux « états de sortie » possibles à travers un type de retour unique.

### `Try`

Autre version d'union disjointe : résultat « nominal », ou exception levée lors de son calcul.

Permet de spécifier des traitements de récupération (_fall-back_) sur des erreurs prévisibles.
```java
Try<LocalDate> dt = Try.exec(() -> DateTimeFormatter.ISO_LOCAL_DATE.parse(input, LocalDate::from))
        .recover(DateTimeParseException.class, x -> {
            LOGGER.warn("unparsable input, using current date", x);
            return LocalDate.now();
        });
```

## Contrat supplémentaire sur `java.util.function.Predicate`

Permettant d'obtenir une description textuelle de la condition qu'il applique (à des fins de traçage).

## Squelette d'une collection immuable

Le lien avec la programmation fonctionnelle est assez ténu, mais c'est toujours une bonne chose de manipuler des objets immuables !

Comme la (re)défintion des méthodes "basiques" dès lors qu'on veut un conteneur immuable est vite pénible, la classe `AbstractImmutableCollection` propose de le faire pour vous. Elle se spécialise via `AbstractImmutableList` et `AbstractImmutableSet`, suivant la route que le JDK avait tracée.

### Et le groupement qui va avec

La classe `GroupedImmutables` permet de grouper de éléments dans des collections immuables, selon un critère libre (mais normalisé, cf. `GroupingKey`).

Elle s'obtient via la méthode `groupBy` définie dans `AbstractImmutableCollection` mais n'admet **pas** de _factory methods_ pour construire une instance à partir d'éléments "en vrac" et d'un _classifier_.  
Ses classes filles peuvent en revanche le proposer.

## Traits

### `Chainable`

Permet d'appliquer plusieurs modifications successives à un même type d'objet, sans recourir à des variables intermédiaires ou un emboîtement inversement chronologique.

Exemple :
```java
RailSolutions solutions = exchangeProcess.searchSolutions(…)
        .then(this::addAncillariesToProposal)
        .sortByDepartureDate() // `then` pas nécessaire car méthode définie dans RailSolutions
        .then(twinTrainHelper::filterTwinTrainSolutions);
```
devrait sinon s'écrire soit
```java
RailSolutions base = exchangeProcess.searchSolutions(…);
RailSolutions ancillariedAndSorted = addAncillariesToProposal(base)
        .sortByDepartureDate();
RailSolutions solutions = twinTrainHelper.filterTwinTrainSolutions(ancillariedAndSorted);
```
soit (lisible /20)
```java
RailSolutions solutions = twinTrainHelper.filterTwinTrainSolutions(
        addAncillariesToProposal(
                exchangeProcess.searchSolutions(…)
        ).sortByDepartureDate()
);
```
