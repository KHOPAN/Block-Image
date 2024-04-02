package com.khopan.blockimage.placer.score;

class EuclideanColorScore implements ColorScore {
	@Override
	public double score(int alphaX, int redX, int greenX, int blueX, int alphaY, int redY, int greenY, int blueY) {
		int red = redX - redY;
		int green = greenX - greenY;
		int blue = blueX - blueY;
		return Math.sqrt(red * red + green * green + blue * blue);
	}
}
