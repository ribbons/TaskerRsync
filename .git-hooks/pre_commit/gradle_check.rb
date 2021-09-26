# frozen_string_literal: true

# Copyright © 2021 Matt Robinson
#
# SPDX-License-Identifier: GPL-3.0-or-later

module Overcommit
  module Hook
    module PreCommit
      class GradleCheck < Base
        def run
          result = execute(command)

          if result.success?
            :pass
          else
            [:fail, result.stdout + result.stderr]
          end
        end
      end
    end
  end
end
