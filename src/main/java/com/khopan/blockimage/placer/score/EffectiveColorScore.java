package com.khopan.blockimage.placer.score;

class EffectiveColorScore implements ColorScore {
	@Override
	public double score(int alphaX, int redX, int greenX, int blueX, int alphaY, int redY, int greenY, int blueY) {
		int redMean = (redX + redY) >> 1;
		int red = redX - redY;
		int green = greenX - greenY;
		int blue = blueX - blueY;
		return Math.sqrt((((512 + redMean) * red * red) >> 8) + 4 * green * green + (((767 - redMean) * blue * blue) >> 8));
	}
}
