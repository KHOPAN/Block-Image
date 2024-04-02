package com.khopan.blockimage.placer.score;

public interface ColorScore {
	public static final ColorScore SIMPLE = new SimpleColorScore();
	public static final ColorScore EFFECTIVE = new EffectiveColorScore();
	public static final ColorScore EUCLIDEAN = new EuclideanColorScore();

	double score(int alphaX, int redX, int greenX, int blueX, int alphaY, int redY, int greenY, int blueY);
}
