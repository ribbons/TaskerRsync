# frozen_string_literal: true

# Copyright © 2025 Matt Robinson
#
# SPDX-License-Identifier: GPL-3.0-or-later

module Overcommit
  module Hook
    module PreCommit
      class Metadata < Base
        README = 'README.md'
        SHORT_DESCRIPTION = 'metadata/en-US/short_description.txt'
        FULL_DESCRIPTION = 'metadata/en-US/full_description.txt'

        def file_error_message(file, error)
          Overcommit::Hook::Message.new(
            :error,
            file,
            nil,
            "#{file}: #{error}"
          )
        end

        def run
          messages = []

          content = included_files.to_h do |file|
            relfile = file.delete_prefix("#{Overcommit::Utils.repo_root}/")
            [relfile, File.read(file)]
          end

          readme = content.delete(README)

          # Remove the first sub-heading and all content after it
          readme.gsub!(/\n\n[^\n]+\n---+\n\n.*\Z/m, '')

          matches = /
            \A[^\n]+\n===+\n\n
            An[ ]Android[ ]app[ ]providing[ ](?<short>[^\n]+\n)
            (?:\n(?<full>.+))?\Z
            /mx.match(readme)

          if matches.nil?
            return [file_error_message(README, 'Unexpected format')]
          end

          unless content[SHORT_DESCRIPTION] == matches[:short].sub(/.$/, '')
            messages << file_error_message(
              SHORT_DESCRIPTION,
              'Does not match README'
            )
          end

          unless content[FULL_DESCRIPTION] == matches[:full].gsub(/ \\$/, '  ')
            messages << file_error_message(
              FULL_DESCRIPTION,
              'Does not match README'
            )
          end

          messages
        end
      end
    end
  end
end
