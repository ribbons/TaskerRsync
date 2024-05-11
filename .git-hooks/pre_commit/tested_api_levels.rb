# frozen_string_literal: true

# Copyright © 2024 Matt Robinson
#
# SPDX-License-Identifier: GPL-3.0-or-later

module Overcommit
  module Hook
    module PreCommit
      class TestedApiLevels < Base
        def run
          messages = []

          applicable_files.each do |file|
            relfile = file.delete_prefix("#{Overcommit::Utils.repo_root}/")
            content = File.read(file)

            min_sdk = content[/^ +minSdk = ([0-9]{2})$/, 1]&.to_i
            target_sdk = content[/^ +targetSdk = ([0-9]{2})$/, 1]&.to_i

            unless min_sdk && target_sdk
              messages << Overcommit::Hook::Message.new(
                :error,
                file,
                nil,
                "#{relfile}: Unable to find minSdk and targetSdk values"
              )

              next
            end

            ci = YAML.load_file("#{Overcommit::Utils.repo_root}/.github/workflows/ci.yml")
            levels = ci['jobs']['test']['strategy']['matrix']['api-level']

            unless levels.include?(target_sdk)
              messages << Overcommit::Hook::Message.new(
                :error,
                file,
                nil,
                "targetSdk API level (#{target_sdk}) is missing from CI instrumented tests matrix"
              )
            end

            unless levels.include?(min_sdk)
              messages << Overcommit::Hook::Message.new(
                :error,
                file,
                nil,
                "minSdk API level (#{min_sdk}) is missing from CI instrumented tests matrix"
              )
            end
          end

          messages
        end
      end
    end
  end
end
