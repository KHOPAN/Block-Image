package com.khopan.minecraft.common.command.argument;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.khopan.minecraft.common.KHOPANCommon;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class FileArgumentType implements ArgumentType<File> {
	private FileArgumentType() {}

	private static final SimpleCommandExceptionType ERROR_INVALID_PATH = new SimpleCommandExceptionType(Component.translatable("error.command.khopancommon.invalid_path"));
	private static final SimpleCommandExceptionType ERROR_PATH_NOT_ABSOLUTE = new SimpleCommandExceptionType(Component.translatable("error.command.khopancommon.path_not_absolute"));
	private static final SimpleCommandExceptionType ERROR_PATH_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("error.command.khopancommon.path_not_found"));
	private static final SimpleCommandExceptionType ERROR_PATH_MUST_BE_FILE = new SimpleCommandExceptionType(Component.translatable("error.command.khopancommon.path_must_be_file"));
	private static final SimpleCommandExceptionType ERROR_EXPECTED_STRING_TERMINATOR = new SimpleCommandExceptionType(Component.translatable("error.command.khopancommon.expected_string_terminator"));

	@Override
	public File parse(StringReader reader) throws CommandSyntaxException {
		int start = reader.getCursor();
		char firstCharacter = reader.peek();
		boolean escape = false;
		boolean close = false;
		char peek;

		if(firstCharacter == '\'' || firstCharacter == '"') {
			escape = true;
			reader.skip();
			start++;
		}

		while(reader.canRead() && ((peek = reader.peek()) != ' ' || (escape && peek != firstCharacter))) {
			reader.skip();

			if(escape && peek == firstCharacter) {
				close = true;
				break;
			}
		}

		if(escape && !close) {
			throw FileArgumentType.ERROR_EXPECTED_STRING_TERMINATOR.createWithContext(reader);
		}

		String filePath = reader.getString().substring(start, reader.getCursor() - (escape ? 1 : 0));
		Path path;

		try {
			path = Paths.get(filePath);
		} catch(Throwable Errors) {
			throw FileArgumentType.ERROR_INVALID_PATH.createWithContext(reader);
		}

		File file = path.toFile();

		if(!file.isAbsolute()) {
			throw FileArgumentType.ERROR_PATH_NOT_ABSOLUTE.createWithContext(reader);
		}

		if(!file.exists()) {
			throw FileArgumentType.ERROR_PATH_NOT_FOUND.createWithContext(reader);
		}

		if(!file.isFile()) {
			throw FileArgumentType.ERROR_PATH_MUST_BE_FILE.createWithContext(reader);
		}

		return file;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		try {
			List<Suggestion> list = new ArrayList<>();
			String input = builder.getInput();
			int inputLength = input.length();
			int start = builder.getStart();
			String remaining = input.substring(start);
			char firstCharacter = remaining.isEmpty() ? '\u0000' : input.charAt(start);
			boolean hasQuote = firstCharacter != '\u0000' && (firstCharacter == '\'' || firstCharacter == '"');
			File[] roots = File.listRoots();

			if(hasQuote) {
				start++;
				remaining = input.substring(start);
			}

			if(roots != null && roots.length > 0) {
				for(int i = 0; i < roots.length; i++) {
					String path = roots[i].getPath();
					int last = path.length() - 1;

					if(path.charAt(last) == '\\') {
						path = path.substring(0, last);
					}

					if(path.startsWith(remaining) && !path.equals(remaining)) {
						list.add(new Suggestion(StringRange.between(start, inputLength), path));
					}
				}
			}

			int index = Math.max(remaining.lastIndexOf('\\'), remaining.lastIndexOf('/'));

			if(index != -1) {
				String directoryPart = remaining.substring(0, index + 1);
				String namePart = remaining.substring(index + 1);
				File directory = new File(directoryPart);
				int directoryLength = directoryPart.length();

				if(directory.isDirectory()) {
					File[] files = directory.listFiles();

					if(files != null && files.length > 0) {
						for(int i = 0; i < files.length; i++) {
							File file = files[i];
							String name = file.getName();

							if(!hasQuote && name.contains(" ")) {
								continue;
							}

							if(name.startsWith(namePart)) {
								boolean addQuote = hasQuote && file.isFile();
								list.add(new Suggestion(StringRange.between(start + directoryLength, inputLength + (addQuote ? 0 : 0)), name + (addQuote ? firstCharacter : "")));
							}
						}
					}
				}
			}

			return CompletableFuture.completedFuture(Suggestions.create(input, list));
		} catch(Throwable Errors) {
			KHOPANCommon.LOGGER.warn("Command suggestion error: " + Errors.getMessage());
			return builder.buildFuture();
		}
	}

	public static FileArgumentType file() {
		return new FileArgumentType();
	}

	public static File getFile(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, File.class);
	}
}
