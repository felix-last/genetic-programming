package core;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import programElements.Addition;
import programElements.Constant;
import programElements.Cosine;
import programElements.Exponential;
import programElements.InputVariable;
import programElements.Multiplication;
import programElements.NaturalLogarithm;
import programElements.Operator;
import programElements.ProgramElement;
import programElements.ProtectedDivision;
import programElements.Sine;
import programElements.Square;
import programElements.SquareRoot;
import programElements.Subtraction;
import programElements.Tangent;
import utils.Utils;

public class GpRun implements Serializable {

	private static final long serialVersionUID = 7L;
	protected static final String INITIAL_INDIVIDUALS_FOLDER = "best";

	// ##### parameters #####
	protected Data data;
	protected ArrayList<ProgramElement> functionSet, terminalSet, fullSet;
	protected int populationSize;
	protected boolean applyDepthLimit;
	protected int maximumDepth;
	protected double crossoverProbability;
	protected boolean printAtEachGeneration;
	protected boolean useIndividualsFromFile;

	// ##### state #####
	protected Random randomGenerator;
	protected int currentGeneration;
	protected Population population;
	protected Individual currentBest;

	public GpRun(Data data) {
		this.data = data;
		initialize();
	}

	protected void initialize() {

		// adds all the functions to the function set
		functionSet = new ArrayList<ProgramElement>();
		functionSet.add(new Addition());
		functionSet.add(new Subtraction());
		functionSet.add(new Multiplication());
		functionSet.add(new ProtectedDivision());
		functionSet.add(new Sine());
		functionSet.add(new Cosine());
		functionSet.add(new Tangent());
		functionSet.add(new Square());
		functionSet.add(new SquareRoot());
		functionSet.add(new Exponential());
		functionSet.add(new NaturalLogarithm());

		// adds all the constants to the terminal set
		// TODO: Other constants?
		terminalSet = new ArrayList<ProgramElement>();
		double[] constants = { -2.0, -1.75, -1.5, -1.25, -1.0, -0.75, -0.5, -0.25, 0.0, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0 };
		for (int i = 0; i < constants.length; i++) {
			terminalSet.add(new Constant(constants[i]));
		}

		// adds all the input variables to the terminal set
		for (int i = 0; i < data.getDimensionality(); i++) {
			terminalSet.add(new InputVariable(i));
		}

		// creates the set which contains all the program elements
		fullSet = new ArrayList<ProgramElement>();
		for (ProgramElement programElement : functionSet) {
			fullSet.add(programElement);
		}
		for (ProgramElement programElement : terminalSet) {
			fullSet.add(programElement);
		}
		
		// TODO: population size, depth limit, max. depth, P(c)
		populationSize = 50;
		applyDepthLimit = true;
		maximumDepth = 20;
		crossoverProbability = 0.00;
		printAtEachGeneration = true;
		useIndividualsFromFile = true;

		randomGenerator = new Random();
		currentGeneration = 0;

		// initialize and evaluate population
		rampedHalfAndHalfInitialization();
		if(this.useIndividualsFromFile) {
			initializeFromFile();
		}
		for (int i = 0; i < populationSize; i++) {
			population.getIndividual(i).evaluate(data);
		}

		updateCurrentBest();
		printState();
		currentGeneration++;
	}

	protected void rampedHalfAndHalfInitialization() {
		// TODO: Max intial depth, change initialization?
		int maximumInitialDepth = 4;
		/*
		 * depth at the root node is 0. this implies that the number of
		 * different depths is equal to the maximumInitialDepth
		 */
		int individualsPerDepth = populationSize / maximumInitialDepth;
		int remainingIndividuals = populationSize % maximumInitialDepth;
		population = new Population();
		int fullIndividuals, growIndividuals;

		for (int depth = 1; depth <= maximumInitialDepth; depth++) {
			if (depth == maximumInitialDepth) {
				fullIndividuals = (int) Math.floor((individualsPerDepth + remainingIndividuals) / 2.0);
				growIndividuals = (int) Math.ceil((individualsPerDepth + remainingIndividuals) / 2.0);
			} else {
				fullIndividuals = (int) Math.floor(individualsPerDepth / 2.0);
				growIndividuals = (int) Math.ceil(individualsPerDepth / 2.0);
			}

			for (int i = 0; i < fullIndividuals; i++) {
				population.addIndividual(full(depth));
			}
			for (int i = 0; i < growIndividuals; i++) {
				population.addIndividual(grow(depth));
			}
		}
	}
	
	protected void initializeFromFile() {
		File folder = new File(INITIAL_INDIVIDUALS_FOLDER);
		if(!folder.isDirectory()) {
			System.out.println("Folder \"" + INITIAL_INDIVIDUALS_FOLDER + "\" does not exist.");
			System.exit(0);
		}
		ArrayList<Individual> individuals = new ArrayList<>();
		for(File file : folder.listFiles()) {
			if(!file.isDirectory()) {
				try {
					individuals.add(
						(Individual) Utils.readObjectFromFile(file.getName())
					);
				}
				catch(ClassCastException e) {
					System.out.println("Wrong input format.");
					throw(e);
				}
			}
		}
		for(Individual individual : individuals) {
			// drop all worst first to avoid dropping imported individuals
			population.dropWorst();
		}
		for(Individual individual : individuals) {
			population.addIndividual(individual);
		}
		
	}

	protected Individual full(int maximumTreeDepth) {
		Individual individual = new Individual();
		fullInner(individual, 0, maximumTreeDepth);
		individual.setDepth(maximumTreeDepth);
		return individual;
	}

	protected void fullInner(Individual individual, int currentDepth, int maximumTreeDepth) {
		if (currentDepth == maximumTreeDepth) {
			ProgramElement randomTerminal = terminalSet.get(randomGenerator.nextInt(terminalSet.size()));
			individual.addProgramElement(randomTerminal);
		} else {
			Operator randomOperator = (Operator) functionSet.get(randomGenerator.nextInt(functionSet.size()));
			individual.addProgramElement(randomOperator);
			for (int i = 0; i < randomOperator.getArity(); i++) {
				fullInner(individual, currentDepth + 1, maximumTreeDepth);
			}
		}
	}

	protected Individual grow(int maximumTreeDepth) {
		Individual individual = new Individual();
		growInner(individual, 0, maximumTreeDepth);
		individual.calculateDepth();
		return individual;
	}

	protected void growInner(Individual individual, int currentDepth, int maximumTreeDepth) {
		if (currentDepth == maximumTreeDepth) {
			ProgramElement randomTerminal = terminalSet.get(randomGenerator.nextInt(terminalSet.size()));
			individual.addProgramElement(randomTerminal);
		} else {
			// equal probability of adding a terminal or an operator
			if (randomGenerator.nextBoolean()) {
				Operator randomOperator = (Operator) functionSet.get(randomGenerator.nextInt(functionSet.size()));
				individual.addProgramElement(randomOperator);
				for (int i = 0; i < randomOperator.getArity(); i++) {
					growInner(individual, currentDepth + 1, maximumTreeDepth);
				}
			} else {
				ProgramElement randomTerminal = terminalSet.get(randomGenerator.nextInt(terminalSet.size()));
				individual.addProgramElement(randomTerminal);
			}
		}
	}

	public void evolve(int numberOfGenerations) {

		// evolve for a given number of generations
		while (currentGeneration <= numberOfGenerations) {
			Population offspring = new Population();

			// generate a new offspring population
			while (offspring.getSize() < population.getSize()) {
				Individual p1, newIndividual;
				p1 = selectParent();
				// apply crossover
				if (randomGenerator.nextDouble() < crossoverProbability) {
					Individual p2 = selectParent();
					newIndividual = applyStandardCrossover(p1, p2);
				}
				// apply mutation
				else {
					newIndividual = applyStandardMutation(p1);
				}

				/*
				 * add the new individual to the offspring population if its
				 * depth is not higher than the maximum (applicable only if the
				 * depth limit is enabled)
				 */
				if (applyDepthLimit && newIndividual.getDepth() > maximumDepth) {
					newIndividual = p1;
					// TODO: avoid bloat
				} else {
					newIndividual.evaluate(data);
				}
				offspring.addIndividual(newIndividual);
			}

			population = selectSurvivors(offspring);
			updateCurrentBest();
			printState();
			currentGeneration++;
		}
	}

	protected void printState() {
		if (printAtEachGeneration) {
			System.out.println("\nGeneration:\t\t" + currentGeneration);
			System.out.printf("Training error:\t\t%.2f\nUnseen error:\t\t%.2f\nSize:\t\t\t%d\nDepth:\t\t\t%d\n",
					currentBest.getTrainingError(), currentBest.getUnseenError(), currentBest.getSize(),
					currentBest.getDepth());
		}
	}

	// tournament selection
	protected Individual selectParent() {
		Population tournamentPopulation = new Population();
		int tournamentSize = (int) (0.10 * population.getSize());
		// TODO: Tournament size
		// TODO: other selection method?
		for (int i = 0; i < tournamentSize; i++) {
			int index = randomGenerator.nextInt(population.getSize());
			tournamentPopulation.addIndividual(population.getIndividual(index));
		}
		return tournamentPopulation.getBest();
	}

	protected Individual applyStandardCrossover(Individual p1, Individual p2) {

		int p1CrossoverStart = randomGenerator.nextInt(p1.getSize());
		int p1ElementsToEnd = p1.countElementsToEnd(p1CrossoverStart);
		int p2CrossoverStart = randomGenerator.nextInt(p2.getSize());
		int p2ElementsToEnd = p2.countElementsToEnd(p2CrossoverStart);

		Individual offspring = p1.selectiveDeepCopy(p1CrossoverStart, p1CrossoverStart + p1ElementsToEnd - 1);

		// add the selected tree from the second parent to the offspring
		for (int i = 0; i < p2ElementsToEnd; i++) {
			offspring.addProgramElementAtIndex(p2.getProgramElementAtIndex(p2CrossoverStart + i), p1CrossoverStart + i);
		}

		offspring.calculateDepth();
		return offspring;
	}

	protected Individual applyStandardMutation(Individual p) {

		int mutationPoint = randomGenerator.nextInt(p.getSize());
		int parentElementsToEnd = p.countElementsToEnd(mutationPoint);
		Individual offspring = p.selectiveDeepCopy(mutationPoint, mutationPoint + parentElementsToEnd - 1);
		int maximumDepth = 4;
		// TODO: max. depth
		Individual randomTree = grow(maximumDepth);

		// add the random tree to the offspring
		for (int i = 0; i < randomTree.getSize(); i++) {
			offspring.addProgramElementAtIndex(randomTree.getProgramElementAtIndex(i), mutationPoint + i);
		}

		offspring.calculateDepth();
		return offspring;
	}

	// keep the best overall + all the remaining offsprings
	protected Population selectSurvivors(Population newIndividuals) {
		Population survivors = new Population();
		Individual bestParent = population.getBest();
		// TODO: different elitism parameters?
		Individual bestNewIndividual = newIndividuals.getBest();
		Individual bestOverall;
		// the best overall is in the current population
		if (bestParent.getTrainingError() < bestNewIndividual.getTrainingError()) {
			bestOverall = bestParent;
		}
		// the best overall is in the offspring population
		else {
			bestOverall = bestNewIndividual;
		}

		survivors.addIndividual(bestOverall);
		for (int i = 0; i < newIndividuals.getSize(); i++) {
			if (newIndividuals.getIndividual(i).getId() != bestOverall.getId()) {
				survivors.addIndividual(newIndividuals.getIndividual(i));
			}
		}
		return survivors;
	}

	protected void updateCurrentBest() {
		currentBest = population.getBest();
	}

	// ##### get's and set's from here on #####

	public Individual getCurrentBest() {
		return currentBest;
	}

	public ArrayList<ProgramElement> getFunctionSet() {
		return functionSet;
	}

	public ArrayList<ProgramElement> getTerminalSet() {
		return terminalSet;
	}

	public ArrayList<ProgramElement> getFullSet() {
		return fullSet;
	}

	public boolean getApplyDepthLimit() {
		return applyDepthLimit;
	}

	public int getMaximumDepth() {
		return maximumDepth;
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public int getCurrentGeneration() {
		return currentGeneration;
	}

	public Data getData() {
		return data;
	}

	public Population getPopulation() {
		return population;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public Random getRandomGenerator() {
		return randomGenerator;
	}

	public boolean getPrintAtEachGeneration() {
		return printAtEachGeneration;
	}

	public void setFunctionSet(ArrayList<ProgramElement> functionSet) {
		this.functionSet = functionSet;
	}

	public void setTerminalSet(ArrayList<ProgramElement> terminalSet) {
		this.terminalSet = terminalSet;
	}

	public void setFullSet(ArrayList<ProgramElement> fullSet) {
		this.fullSet = fullSet;
	}

	public void setApplyDepthLimit(boolean applyDepthLimit) {
		this.applyDepthLimit = applyDepthLimit;
	}

	public void setMaximumDepth(int maximumDepth) {
		this.maximumDepth = maximumDepth;
	}

	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	public void setPrintAtEachGeneration(boolean printAtEachGeneration) {
		this.printAtEachGeneration = printAtEachGeneration;
	}
}
